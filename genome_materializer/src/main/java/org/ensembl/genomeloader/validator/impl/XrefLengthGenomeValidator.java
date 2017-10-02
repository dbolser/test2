package org.ensembl.genomeloader.validator.impl;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ensembl.genomeloader.model.CrossReferenced;
import org.ensembl.genomeloader.model.DatabaseReference;
import org.ensembl.genomeloader.model.DatabaseReferenceType;
import org.ensembl.genomeloader.model.Gene;
import org.ensembl.genomeloader.model.Genome;
import org.ensembl.genomeloader.model.GenomicComponent;
import org.ensembl.genomeloader.model.Protein;
import org.ensembl.genomeloader.model.RnaTranscript;
import org.ensembl.genomeloader.model.Rnagene;
import org.ensembl.genomeloader.model.Transcript;
import org.ensembl.genomeloader.validator.GenomeValidationException;
import org.ensembl.genomeloader.validator.GenomeValidator;

public class XrefLengthGenomeValidator implements GenomeValidator {

    public class XrefLengthValidationException extends GenomeValidationException {

        private static final long serialVersionUID = 1L;
        private final DatabaseReference reference;

        public XrefLengthValidationException(String message, DatabaseReference reference) {
            super(message);
            this.reference = reference;
        }

        public DatabaseReference getReference() {
            return reference;
        }

    }

    private static final int MAX_XREF_LEN = 512;

    private final Log log = LogFactory.getLog(XrefLengthGenomeValidator.class);

    public XrefLengthGenomeValidator() {
    }

    public void validateGenome(Genome genome) throws GenomeValidationException {
        log.info("Validating references for " + genome.getName());
        for (final GenomicComponent component : genome.getGenomicComponents()) {
            for (final Gene gene : component.getGenes()) {
                validateXrefs(gene);
                for (final Protein protein : gene.getProteins()) {
                    validateXrefs(protein);
                    for (final Transcript transcript : protein.getTranscripts()) {
                        validateXrefs(transcript);
                    }
                }
            }
            for (final Rnagene gene : component.getRnagenes()) {
                validateXrefs(gene);
                for (final RnaTranscript transcript : gene.getTranscripts()) {
                    validateXrefs(transcript);
                }
            }
        }
        log.info("Completed validating references for " + genome.getName());
    }

    private void validateXrefs(CrossReferenced obj) throws GenomeValidationException {
        for (final DatabaseReference reference : obj.getDatabaseReferences()) {
            if (reference.getPrimaryIdentifier().length() > MAX_XREF_LEN) {
                throw new XrefLengthValidationException("Database reference " + reference.getIdString()
                        + " has primary identifier >" + MAX_XREF_LEN + " characters", reference);
            }
        }
    }

    public static void removeXrefs(Genome genome, DatabaseReferenceType... refTypes) {
        for (final GenomicComponent component : genome.getGenomicComponents()) {
            for (final Gene gene : component.getGenes()) {
                removeXrefs(gene, refTypes);
                for (final Protein protein : gene.getProteins()) {
                    removeXrefs(protein, refTypes);
                    for (final Transcript transcript : protein.getTranscripts()) {
                        removeXrefs(transcript, refTypes);
                    }
                }
            }
            for (final Rnagene gene : component.getRnagenes()) {
                removeXrefs(gene, refTypes);
                for (final RnaTranscript transcript : gene.getTranscripts()) {
                    removeXrefs(transcript, refTypes);
                }
            }
        }
    }

    public static void removeXrefs(CrossReferenced obj, DatabaseReferenceType... refTypes) {
        final Iterator<DatabaseReference> iterator = obj.getDatabaseReferences().iterator();
        while (iterator.hasNext()) {
            final DatabaseReferenceType type = iterator.next().getDatabaseReferenceType();
            for (final DatabaseReferenceType refType : refTypes) {
                if (refType.equals(type)) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

}
