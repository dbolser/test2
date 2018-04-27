Overview
========
Once a `Genome` instance has been produced by `EnaGenomeMaterializer`, it is passed to an instance of `GenomeProcessor` for secondary processing. This is currently `EnaGenomeProcessor` which delegates to a series of other processors, found in `org.ensembl.genomeloader.materializer.processors`. The current processors are listed below.

Processors
==========

The first group of processors deal with the genome as materialized from ENA:
* `ComponentSortingProcessor` - Processor that sorts components according to specific rules. This is required to set the karyotype rank.
* `PubMedCentralProcessor` - Processor that converts PMC xrefs from ENA into PubMed IDs for consistency with other Ensembl entries
* `LocationOverlapProcessor` - Processor to identify where proteins have internal overlaps and resolve them. This is where INSDC locations have overlapping joins which Ensembl cannot handle. These are converted into non-overlapping exons with sequence modifiers.
* `LocusTagMergeProcessor` - Processor that identifies genes from ENA which share the same locus tag and attempts to merge them (either as origin-split genes, or as alternative transcripts)
* `AltTranslationProcessor` - Processor that identifies proteins belonging to the same gene that share the same uniprot accession and merges the transcript
* `AssemblyContigProcessor` - Processor that uses assembly information for CON sequences to replicate assembly in an Ensembl-compatible way. 
* `MetaDataProcessor` - Processor to set additional metadata needed for the load. These items include names and dates, and the provider.

If a URI is supplied for the ENA Oracle database (see `CONFIG.md`), `ConXrefProcessor` is run. This deals with CON entries, where the dumped entries from the browser do not contain cross-references, which instead must be added from the ENA Oracle database.

If a URI is supplied for the UniParc Oracle database (see `CONFIG.md`), `UpiGenomeProcessor` is run to add UniParc accessions using the ENA protein_id accessions as keys. This is essential for subsequently adding InterPro entries.

If a URI is supplied for the UniProt Oracle database (see `CONFIG.md`), a sequence of processors is run:
* `UniProtDescriptionGenomeProcessor` - Processor that adds missing descriptions to UniProt xrefs. This is because the ENA records just contain the accession, but Ensembl requires the description
* `UniProtXrefGenomeProcessor` - Processor to add transitive xrefs from UniProt based on UniProt mappings. Note that only xrefs from databases listed in the whitelist file are included.
* `UniProtECGenomeProcessor` - Specialised processor to add EC numbers based on UniProt xrefs

If a URI is supplied for the InterPro Oracle database (see `CONFIG.md`), a sequence of processors is run:
*  UpiInterproGenomeProcessor - map InterPro features onto proteins based on mapped UniParc accession
* InterproPathwayGenomeProcessor - add pathway cross-references from InterPro based on associated InterPro cross-references for the genome

If a URI is supplied for the RFAM MySQL database (see `CONFIG.md`), `RfamProcessor` is run to add RNA genes from the RFAM database.

Finally, there are two optional processors:
- `ComponentAccessionNamingProcessor` - run if `useAccessionsForNames` (see `CONFIG.md`) is true. This avoids problems with trying to work out the name of each component from the description and simply uses the accession instead.
- `TrackingRefRemovalProcessor` - run if `loadTrackingReferences` (see `CONFIG.md`) is false. This removes the large ENA_FEATURE tracking references from genes etc.

Lastly, there is one more processor that is not currently used. `IdMappingEnaGenomeProcessor` attempts to detect, remove and resolve duplicate stable identifiers by invoking `TypeAwareDuplicateIdProcessor` and `IdMappingProcessor`. This may be best left for post-load as assignment of identifiers requires an external database to store mappings but could be used here if required.