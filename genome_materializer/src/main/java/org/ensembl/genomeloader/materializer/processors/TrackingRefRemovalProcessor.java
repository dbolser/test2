package org.ensembl.genomeloader.materializer.processors;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ensembl.genomeloader.model.CrossReferenced;
import org.ensembl.genomeloader.model.DatabaseReference;
import org.ensembl.genomeloader.model.Gene;
import org.ensembl.genomeloader.model.Genome;
import org.ensembl.genomeloader.model.GenomicComponent;
import org.ensembl.genomeloader.model.Protein;
import org.ensembl.genomeloader.model.RnaTranscript;
import org.ensembl.genomeloader.model.Rnagene;
import org.ensembl.genomeloader.model.Transcript;

/**
 * Processor to remove the artificial xrefs added to features so they can be
 * traced back to original ENA features. These xrefs are quite large and not
 * always helpful.
 * 
 * @author dstaines
 *
 */
public class TrackingRefRemovalProcessor implements GenomeProcessor {

    private final Log log = LogFactory.getLog(this.getClass());

    @Override
    public void processGenome(Genome genome) {
        log.info("Removing tracking references for " + genome.getId());
        for (GenomicComponent component : genome.getGenomicComponents()) {
            log.debug("Removing for genes");
            for (Gene g : component.getGenes()) {
                removeRefs(g);
                for (Protein p : g.getProteins()) {
                    removeRefs(p);
                    for (Transcript t : p.getTranscripts()) {
                        removeRefs(t);
                    }
                }
            }
            log.debug("Removing for RNA genes");
            for (Rnagene g : component.getRnagenes()) {
                removeRefs(g);
                for (RnaTranscript t : g.getTranscripts()) {
                    removeRefs(t);
                }
            }
            log.debug("Removing for features");
            removeRefs(component.getFeatures());
            log.debug("Removing for repeats");
            removeRefs(component.getRepeats());
        }
        log.info("Finished removing tracking references for " + genome.getId());
    }

    protected void removeRefs(Collection<? extends CrossReferenced> objs) {
        for (CrossReferenced obj : objs) {
            removeRefs(obj);
        }
    }

    protected void removeRefs(CrossReferenced obj) {
        Iterator<DatabaseReference> refI = obj.getDatabaseReferences().iterator();
        while (refI.hasNext()) {
            DatabaseReference ref = refI.next();
            if ("ENA_FEATURE".equals(ref.getDatabaseReferenceType().getDbName())) {
                refI.remove();
            }
        }
    }

}
