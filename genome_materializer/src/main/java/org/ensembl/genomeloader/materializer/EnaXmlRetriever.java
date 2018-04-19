package org.ensembl.genomeloader.materializer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.ensembl.genomeloader.materializer.executor.FileLockExecutor;
import org.ensembl.genomeloader.materializer.executor.SimpleExecutor;
import org.ensembl.genomeloader.materializer.impl.MaterializationUncheckedException;
import org.ensembl.genomeloader.util.templating.TemplateBuilder;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblEntryReader;
import uk.ac.ebi.embl.flatfile.writer.xml.XmlEntryWriter;

/**
 * This class retrieves entries from ENA as XML. The retrieval requires
 * different approaches for different entry types due to a decision by ENA to
 * not serve WGS entries from the browser. For more information see :
 * https://www.ebi.ac.uk/about/news/service-news/forthcoming-changes-wgs-and-tsa-sequences
 * <p/>
 * For non-WGS entries, the following approach is taken:
 * <ul>
 * <li>check if file already retrieved and return if it is</li>
 * <li>otherwise retrieve the flatfile record from ENA</li>
 * <li>parse to an XML file</li>
 * <li>return the XML file path</li>
 * </ul>
 * <p/>
 * For WGS entries: - determine the WGS set -
 * <ul>
 * <li>check if file already retrieved and return if it is</li>
 * <li>otherwise retrieve the complete WGS set from ENA</li>
 * <li>parse each entry to an XML file and store the path for each
 * accession</li>
 * <li>return the XML file path for that accession</li>
 * </ul>
 * 
 * @author dstaines
 *
 */
public class EnaXmlRetriever {

    @FunctionalInterface
    public interface CheckedConsumer<T> {
        void consume(T t) throws IOException;
    }

    private final static int MAX_TRIES = 3;
    private final static long SLEEP_TIME = 30000;
    private final static Pattern WGS_ACCESSION = Pattern.compile("([A-Z]{4}[0-9]{2})([0-9]{6,8})");

    private final Map<String, File> files = new HashMap<>();
    private final Set<String> wgs = new HashSet<>();
    private final File workingDir;
    private final String enaUrl;
    private final Executor executor;
    private Log log;

    public EnaXmlRetriever(String enaUrl) {
        this(new SimpleExecutor(), enaUrl, getTempDir());
    }

    public EnaXmlRetriever(Executor executor, String enaUrl) {
        this(executor, enaUrl, getTempDir());
    }

    private static File getTempDir() {
        try {
            return Files.createTempDirectory("ENA").toFile();
        } catch (IOException e) {
            throw new MaterializationUncheckedException("Could not create temporary directory", e);
        }
    }

    public EnaXmlRetriever(Executor executor, String enaUrl, File workingDir) {
        this.workingDir = workingDir;
        this.enaUrl = enaUrl;
        this.executor = executor;
    }

    private Log getLog() {
        if (log == null) {
            log = LogFactory.getLog(EnaXmlRetriever.class);
        }
        return log;
    }

    /**
     * @param accession
     *            of entry to retrieve
     * @return file containing entry in XML format
     */
    public File getFileForEntry(String accession) {

        getLog().info("Retrieving entry " + accession);
        File f = files.get(accession);
        if (f == null) {
            Matcher m = WGS_ACCESSION.matcher(accession);
            if (m.matches()) {
                String root = m.group(1);
                if (!wgs.contains(root)) {
                    // lazily download the complete WGS set in batch
                    loadWgsFiles(root);
                    wgs.add(root);
                }
                f = files.get(accession);
            } else {
                f = getStandardFile(accession);
                files.put(accession, f);
            }
        }

        return f;
    }

    protected File getStandardFile(String accession) {
        String url = TemplateBuilder.template(this.enaUrl, "ac", accession);
        File f = getFile(accession);
        f.deleteOnExit();
        getLog().debug("Downloading standard entry " + accession);
        download(url, is -> {
            EmblEntryReader eReader = new EmblEntryReader(new BufferedReader(new InputStreamReader(is)));
            ValidationResult read = eReader.read();
            if (read.isHasReportMessage()) {
                log.warn(read.getReportMessage());
            }
            // write entry to disk as XML
            XmlEntryWriter writer = new XmlEntryWriter(eReader.getEntry());
            writer.write(new FileWriter(f));
        });
        return f;
    }

    protected void loadWgsFiles(String root) {
        getLog().debug("Downloading WGS set " + root);
        // URL requires a set=true argument
        String url = TemplateBuilder.template(this.enaUrl, "ac", root) + "&set=true";
        getLog().debug("Dwonloading " + url);
        download(url, is -> {
            getLog().debug("Parsing stream to entries");
            EmblEntryReader eReader = new EmblEntryReader(
                    new BufferedReader(new InputStreamReader(new GZIPInputStream(is))));
            // loop until all entries found
            do {
                getLog().debug("Reading next entry");
                ValidationResult read = eReader.read();
                if (read.isHasReportMessage()) {
                    log.warn(read.getReportMessage());
                }
                if (eReader.isEntry()) {
                    Entry entry = eReader.getEntry();
                    String entryAcc = entry.getPrimaryAccession();
                    getLog().debug("Entry " + entryAcc + " found");
                    File f = getFile(entryAcc);
                    getLog().debug("Writing to file " + f.getPath());
                    f.deleteOnExit();
                    // write entry to disk as XML
                    XmlEntryWriter writer = new XmlEntryWriter(entry);
                    writer.write(new FileWriter(f));
                    files.put(entryAcc, f);
                }
            } while (eReader.isEntry());
            getLog().debug("Completed downloading WGS set " + root);

        });
    }

    /**
     * Derive a suitable file name for the entry. Uses the numeric portion of
     * the accession to ensure files are no more than 1000 per folder.
     * 
     * @param accession
     *            primary acc of accession
     * @return file for entry
     */
    protected File getFile(String accession) {
        int accNo = Integer.parseInt(accession.replaceAll("[^0-9]+", "")) / 1000;
        File dir = new File(workingDir, String.valueOf(accNo));
        dir.mkdirs();
        dir.deleteOnExit();
        return new File(dir, accession + ".xml");
    }

    /**
     * Generic method to retrieve a stream from a URL, using locking and
     * sleep-retries. Input stream is passed to a consumer
     * 
     * @param url
     * @param consumer
     *            function to consume stream
     */
    protected void download(String url, CheckedConsumer<InputStream> consumer) {

        try {
            executor.execute(new Runnable() {
                public void run() {
                    int tries = 0;
                    while (tries < MAX_TRIES) {
                        CloseableHttpResponse response = null;
                        try {
                            getLog().debug("Downloading " + url);
                            CloseableHttpClient httpclient = HttpClients.createDefault();
                            HttpGet httpget = new HttpGet(url);
                            response = httpclient.execute(httpget);
                            consumer.consume(response.getEntity().getContent());
                            tries = MAX_TRIES;
                            break;
                        } catch (MaterializationUncheckedException | IOException e) {
                            if (tries++ < MAX_TRIES) {
                                getLog().warn("Could not parse ENA record from URL " + url + ": retrying", e);
                                try {
                                    Thread.sleep(SLEEP_TIME);
                                } catch (InterruptedException e1) {
                                    getLog().warn("Woke up from sleep", e1);
                                }
                            } else {
                                throw new EnaParsingException("Could not parse ENA record from URL " + url, e);
                            }
                        } finally {
                            if (response != null) {
                                try {
                                    response.close();
                                } catch (IOException e) {
                                    // do nothing
                                }
                            }
                        }
                    }
                }
            });
        } finally {

        }

    }

    public static final void main(String[] args) throws IOException {
        EnaXmlRetriever r = new EnaXmlRetriever(new FileLockExecutor("/tmp/lockdir", 10),
                "https://www.ebi.ac.uk/ena/data/view/$ac$&display=text&expanded=true", new File("/tmp/testena"));
        for (String a : args) {
            System.out.println(r.getFileForEntry(a).getAbsolutePath());
        }
    }

}
