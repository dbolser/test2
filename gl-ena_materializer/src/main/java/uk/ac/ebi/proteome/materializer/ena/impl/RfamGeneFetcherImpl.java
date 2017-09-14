package uk.ac.ebi.proteome.materializer.ena.impl;

import java.sql.SQLException;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ebi.proteome.genomebuilder.model.DatabaseReferenceType;
import uk.ac.ebi.proteome.genomebuilder.model.Rnagene;
import uk.ac.ebi.proteome.genomebuilder.model.impl.DatabaseReferenceImpl;
import uk.ac.ebi.proteome.genomebuilder.model.impl.DelegatingEntityLocation;
import uk.ac.ebi.proteome.genomebuilder.model.impl.RnaTranscriptImpl;
import uk.ac.ebi.proteome.genomebuilder.model.impl.RnageneImpl;
import uk.ac.ebi.proteome.materializer.ena.RfamGeneFetcher;
import uk.ac.ebi.proteome.services.sql.ROResultSet;
import uk.ac.ebi.proteome.util.biojava.LocationUtils;
import uk.ac.ebi.proteome.util.sql.RowMapper;
import uk.ac.ebi.proteome.util.sql.SqlServiceTemplate;

public class RfamGeneFetcherImpl implements RfamGeneFetcher {
    private static final String NC_RNA = "ncRNA";
    private final static String ANALYSIS = "RFAM_GENES";
    private final static String RFAM_SQL = "select rfam_acc,rfam_id,description,family.type,"
            + "seq_start,seq_end,case when seq_start>seq_end then -1 else 1 end "
            + "from full_region join family using (rfam_acc) " + "where rfamseq_acc=? "
            + "and family.type like 'Gene%' " + "and is_significant=1";

    private Log log;

    protected Log getLog() {
        if (log == null) {
            log = LogFactory.getLog(this.getClass());
        }
        return log;
    }

    private final RowMapper<Rnagene> mapper = new RowMapper<Rnagene>() {

        public Rnagene mapRow(ROResultSet resultSet, int position) throws SQLException {
            final RnageneImpl gene = new RnageneImpl();
            gene.setAnalysis(ANALYSIS);
            // type contains a biotype-like string which can be parsed out
            final String[] types = resultSet.getString(4).split("; ?");
            if (types.length > 1) {
                gene.setBiotype(types[1]);
            }
            if (StringUtils.isEmpty(gene.getBiotype())) {
                gene.setBiotype(NC_RNA);
            }
            gene.setName(resultSet.getString(2));
            gene.setDescription(resultSet.getString(3));
            gene.setLocation(new DelegatingEntityLocation(LocationUtils.buildSimpleLocation(resultSet.getInt(5),
                    resultSet.getInt(6), resultSet.getInt(7) == 1)));
            gene.addDatabaseReference(
                    new DatabaseReferenceImpl(rfamType, resultSet.getString(1), resultSet.getString(2)));
            final RnaTranscriptImpl t = new RnaTranscriptImpl();
            t.setName(gene.getName());
            t.setDescription(gene.getDescription());
            t.setLocation(gene.getLocation());
            t.setBiotype(gene.getBiotype());
            t.setAnalysis(gene.getAnalysis());
            gene.addTranscript(t);
            return gene;
        }
    };

    private final SqlServiceTemplate rfamSrv;
    private final DatabaseReferenceType rfamType;

    public RfamGeneFetcherImpl(SqlServiceTemplate rfamSrv, DatabaseReferenceType rfamType) {
        this.rfamSrv = rfamSrv;
        this.rfamType = rfamType;
        if (this.rfamType == null) {
            throw new MaterializationUncheckedException("Null type passed for RFAM");
        }
    }

    public Collection<Rnagene> fetchGenes(String accession) {
        getLog().debug("Retrieving RFAM genes for " + accession);
        return rfamSrv.queryForList(RFAM_SQL, mapper, accession);
    }

}
