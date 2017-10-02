/*
 * Copyright [2009-2014] EMBL-European Bioinformatics Institute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ensembl.genomeloader.validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.ensembl.genomeloader.materializer.EnaGenomeConfig;
import org.ensembl.genomeloader.metadata.GenomicComponentMetaData.GenomicComponentType;
import org.ensembl.genomeloader.model.DatabaseReferenceType;
import org.ensembl.genomeloader.model.Genome;
import org.ensembl.genomeloader.model.GenomicComponent;
import org.ensembl.genomeloader.validator.impl.AssemblyValidator;
import org.ensembl.genomeloader.validator.impl.ComponentNameValidator;
import org.ensembl.genomeloader.validator.impl.ComponentSizeValidator;
import org.ensembl.genomeloader.validator.impl.DelegatingGenomeValidator;
import org.ensembl.genomeloader.validator.impl.EntityLocationValidator;
import org.ensembl.genomeloader.validator.impl.GeneCountValidator;
import org.ensembl.genomeloader.validator.impl.GenomeDescriptionValidator;
import org.ensembl.genomeloader.validator.impl.MixedCoordSystemValidator;
import org.ensembl.genomeloader.validator.impl.UniqueComponentNameValidator;
import org.ensembl.genomeloader.validator.impl.XrefLengthGenomeValidator;
import org.ensembl.genomeloader.validator.impl.XrefLengthGenomeValidator.XrefLengthValidationException;
import org.ensembl.genomeloader.xrefregistry.DatabaseReferenceTypeRegistry;
import org.ensembl.genomeloader.xrefregistry.impl.XmlDatabaseReferenceTypeRegistry;

public class EnaGenomeValidator extends DelegatingGenomeValidator {

    protected static Collection<GenomeValidator> getValidators(EnaGenomeConfig config) {
        final Collection<GenomeValidator> validators = new ArrayList<>();
        validators.add(new GenomeDescriptionValidator());
        if (!config.isAllowEmptyGenomes())
            validators.add(new GeneCountValidator(config.getMinGeneCount()));
        validators.add(new UniqueComponentNameValidator());
        validators.add(new ComponentNameValidator());
        if (!config.isAllowMixedCoordSystems())
            validators.add(new MixedCoordSystemValidator());
        validators.add(new ComponentSizeValidator());
        validators.add(new EntityLocationValidator());
        validators.add(new XrefLengthGenomeValidator());
        validators.add(new AssemblyValidator());
        return validators;
    }

    final DatabaseReferenceType geneType;
    final DatabaseReferenceType transcriptType;
    final DatabaseReferenceType proteinType;

    public EnaGenomeValidator(EnaGenomeConfig config) {
        super(getValidators(config));
        final DatabaseReferenceTypeRegistry registry = new XmlDatabaseReferenceTypeRegistry();
        geneType = registry.getTypeForQualifiedName("ENA_FEATURE", "GENE");
        transcriptType = registry.getTypeForQualifiedName("ENA_FEATURE", "TRANSCRIPT");
        proteinType = registry.getTypeForQualifiedName("ENA_FEATURE", "PROTEIN");
    }

    @Override
    public void validateGenome(Genome genome) throws GenomeValidationException {
        for (final GenomeValidator validator : validators) {
            try {
                validator.validateGenome(genome);
            } catch (final ComponentSizeValidationException e) {
                getLog().warn("Replacing component lengths as problem found: " + e.getMessage());
                for (final GenomicComponent c : genome.getGenomicComponents()) {
                    c.getMetaData().setLength((int) c.getSequence().getLength());
                }
            } catch (final ComponentNameValidationException e) {
                for (final Entry<String, List<GenomicComponent>> entry : e.getProblems().entrySet()) {
                    for (final GenomicComponent c : entry.getValue()) {
                        getLog().warn("Replacing non-unique component name " + c.getMetaData().getName()
                                + " with accession " + c.getAccession());

                        c.getMetaData().setDescription("Supercontig " + c.getAccession());
                        c.getMetaData().setName(c.getAccession());
                        if (!c.getMetaData().getComponentType().equals(GenomicComponentType.CONTIG)) {
                            c.getMetaData().setComponentType(GenomicComponentType.SUPERCONTIG);
                        }
                        getLog().warn("Component name is now " + c.getMetaData().getName() + " for accession "
                                + c.getAccession());
                    }
                }
            } catch (final ComponentNameLengthValidationException e) {
                for (final GenomicComponent c : e.getProblems()) {
                    getLog().warn("Replacing over-long component name " + c.getMetaData().getName() + " with accession "
                            + c.getAccession());

                    c.getMetaData().setDescription("Supercontig " + c.getAccession());
                    c.getMetaData().setName(c.getAccession());
                    if (!c.getMetaData().getComponentType().equals(GenomicComponentType.CONTIG)) {
                        c.getMetaData().setComponentType(GenomicComponentType.SUPERCONTIG);
                    }
                }
            } catch (final XrefLengthValidationException e) {
                final DatabaseReferenceType type = e.getReference().getDatabaseReferenceType();

                if (type.equals(geneType) || type.equals(transcriptType) || type.equals(proteinType)) {
                    getLog().warn("Database reference of type " + type.getDisplayName()
                            + " has ID with >512 characters - stripping out all tracking references");
                    XrefLengthGenomeValidator.removeXrefs(genome,
                            new DatabaseReferenceType[] { geneType, proteinType, transcriptType });
                } else {
                    throw e;
                }
            } finally {
            }
        }
    }

}
