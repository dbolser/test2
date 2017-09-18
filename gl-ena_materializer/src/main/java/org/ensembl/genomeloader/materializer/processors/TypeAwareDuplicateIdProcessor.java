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

package org.ensembl.genomeloader.materializer.processors;

import java.util.Map;
import java.util.Set;

import org.ensembl.genomeloader.materializer.DuplicateIdException;
import org.ensembl.genomeloader.materializer.EnaGenomeConfig;
import org.ensembl.genomeloader.model.Genome;
import org.ensembl.genomeloader.model.Identifiable;
import org.ensembl.genomeloader.model.Protein;
import org.ensembl.genomeloader.model.impl.GeneImpl;
import org.ensembl.genomeloader.model.impl.ProteinImpl;
import org.ensembl.genomeloader.model.impl.RnaTranscriptImpl;
import org.ensembl.genomeloader.model.impl.RnageneImpl;
import org.ensembl.genomeloader.model.impl.TranscriptImpl;
import org.ensembl.genomeloader.services.sql.SqlService;
import org.ensembl.genomeloader.util.collections.CollectionUtils;
import org.ensembl.genomeloader.xrefregistry.DatabaseReferenceTypeRegistry;

/**
 * Implementation of {@link DuplicateIdProcessor} that will discard genomes with
 * duplicate IDs for objects of the same type, but where one protein_coding and
 * one non-protein_coding have them, the id is preserved for the protein but
 * rejected for the other types.
 * 
 * @author dstaines
 * 
 */
public class TypeAwareDuplicateIdProcessor extends DuplicateIdProcessor {

    public TypeAwareDuplicateIdProcessor(EnaGenomeConfig config) {
        super(config);
    }

    @Override
    protected int handleDuplicates(Genome genome, Map<String, Set<Identifiable>> dups) {
        int nDuplicate = 0;
        for (Set<Identifiable> ids : dups.values()) {
            if (ids.size() > 1) {
                Set<String> types = CollectionUtils.createHashSet();
                Set<String> idStrs = CollectionUtils.createHashSet();
                int duplicates = 0;
                for (Identifiable id : ids) {
                    if (!allowDuplicate(id)) {
                        types.add(id.getClass().getSimpleName());
                        idStrs.add(id.getIdentifyingId());
                        duplicates++;
                    } else {
                        // where we allow duplicates, we'll just blow away the
                        // existing ID
                        id.setIdentifyingId(null);
                    }
                }
                if (duplicates > 1) {
                    throw new DuplicateIdException("Genome " + genome.getName() + " (ID " + genome.getId()
                            + ") contains duplicate IDs " + idStrs + " for objects of type " + types);
                }
                nDuplicate += ids.size();
            }
        }
        return nDuplicate;
    }

    private boolean allowDuplicate(Identifiable id) {
        // allow RNAgenes
        if (RnageneImpl.class.isAssignableFrom(id.getClass())) {
            return true;
        }
        // allow RNAgenes
        if (RnaTranscriptImpl.class.isAssignableFrom(id.getClass())) {
            return true;
        }
        // allow pseudogenes
        if (GeneImpl.class.isAssignableFrom(id.getClass()) && ((GeneImpl) id).isPseudogene()) {
            return true;
        }
        // allow pseudo-proteins
        if (ProteinImpl.class.isAssignableFrom(id.getClass()) && ((ProteinImpl) id).isPseudo()) {
            return true;
        }
        // allow pseudo-transcripts
        if (TranscriptImpl.class.isAssignableFrom(id.getClass())) {
            boolean isPseudo = false;
            for (Protein p : ((TranscriptImpl) id).getProteins()) {
                if (p.isPseudo()) {
                    isPseudo = true;
                    break;
                }
            }
            if (isPseudo) {
                return true;
            }
        }
        return false;
    }

}
