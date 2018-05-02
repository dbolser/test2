Bad Locations
=============

Error:
```
2018-05-01 15:14:48,353 [main] INFO  materializer.DumpGenome - Validating genome for GCA_001625215
2018-05-01 15:14:48,353 [main] INFO  materializer.EnaGenomeMaterializer - Validating genome Daucus carota subsp. sativus
Exception in thread "main" org.ensembl.genomeloader.materializer.impl.MaterializationUncheckedException: Failed to validate genome Daucus carota subsp. sativus
        at org.ensembl.genomeloader.materializer.EnaGenomeMaterializer.validateGenome(EnaGenomeMaterializer.java:113)
        at org.ensembl.genomeloader.materializer.DumpGenome.materializeGenome(DumpGenome.java:115)
        at org.ensembl.genomeloader.materializer.DumpGenome.materializeGenome(DumpGenome.java:101)
        at org.ensembl.genomeloader.materializer.DumpGenome.dumpGenome(DumpGenome.java:89)
        at org.ensembl.genomeloader.materializer.DumpGenome.main(DumpGenome.java:71)
Caused by: org.ensembl.genomeloader.validator.GenomeValidationException: Location join(complement(134385..134612),complement(132314..133531),complement(150474..150495),93418..93812,95000..95149)(ANNOTATED),insertions=[],exceptions=[] has start>end on linear component
        at org.ensembl.genomeloader.validator.impl.EntityLocationValidator.validateLocation(EntityLocationValidator.java:90)
        at org.ensembl.genomeloader.validator.impl.EntityLocationValidator.validateGenome(EntityLocationValidator.java:65)
        at org.ensembl.genomeloader.validator.EnaGenomeValidator.validateGenome(EnaGenomeValidator.java:77)
        at org.ensembl.genomeloader.materializer.EnaGenomeMaterializer.validateGenome(EnaGenomeMaterializer.java:111)
        ... 4 more
```

GenomeLoader does its very best to try and represent locations from ENA in a way that Ensembl can work with. This is done by `LocationOverlapProcessor` which uses some very complex logic from `ModelUtils` to resolve locations where exons overlap. This doesn't always succeed, so there is a validator, `EntityLocationValidator`, which does some basic checking on the resolved location. In this case, that checking indicates the location couldn't be turned into something Ensembl-friendly.

The solution here was to add a config flag, `skipBrokenLocations`, which allows genes which fail the check to be removed from the genome.       

Description Parsing
===================

Error:
```
2018-05-01 10:38:42,177 [main] INFO  impl.AssemblyValidator - Checking assembly for GCA_000005505
Exception in thread "main" org.ensembl.genomeloader.materializer.impl.MaterializationUncheckedException: Failed to validate genome Brachypodium distachyon str. Bd21
        at org.ensembl.genomeloader.materializer.EnaGenomeMaterializer.validateGenome(EnaGenomeMaterializer.java:113)
        at org.ensembl.genomeloader.materializer.DumpGenome.materializeGenome(DumpGenome.java:115)
        at org.ensembl.genomeloader.materializer.DumpGenome.materializeGenome(DumpGenome.java:101)
        at org.ensembl.genomeloader.materializer.DumpGenome.dumpGenome(DumpGenome.java:89)
        at org.ensembl.genomeloader.materializer.DumpGenome.main(DumpGenome.java:71)
Caused by: org.ensembl.genomeloader.validator.GenomeValidationException: Component CM000880 has the same type (SUPERCONTIG) as child component KZ622961
        at org.ensembl.genomeloader.validator.impl.AssemblyValidator.validateGenome(AssemblyValidator.java:75)
        at org.ensembl.genomeloader.validator.EnaGenomeValidator.validateGenome(EnaGenomeValidator.java:77)
        at org.ensembl.genomeloader.materializer.EnaGenomeMaterializer.validateGenome(EnaGenomeMaterializer.java:111)
        ... 4 more
Could not execute java  -jar /hps/cstor01/nobackup/crop_genomics/dbolser/GenomeLoader/GCA_000005505.4/scripts/../genome_materializer/build/libs/genome_materializer-1.0.jar -c /hps/cstor01/nobackup/crop_genomics/dbolser/GenomeL
oader/GCA_000005505.4/scripts/../enagenome_config.xml -s GCA_000005505 -f /tmp/SIGbhE88Ei/GCA_000005505.json: 256 at ./scripts/load_genome.pl line 306.
```

This is caused by the description parser `DefaultGenomicComponentDescriptionHandler` incorrectly identifying both the chromosome and its child scaffold as a chromosome with the same name. These duplicates are then picked up by the `UniqueComponentNameValidator` which decides that they should just be supercontigs with the accession as the name. However, this now means that a supercontig is the parent of another superconfig, which fails the `AssemblyValidator` step as it would not work with Ensembl. The solution here is to ensure that names are parsed correctly by `DefaultGenomicComponentDescriptionHandler`, though this code is increasinly baroque. The situation is confused more by `SourceParser` also picking up the `chromosome` feature key which is also sometimes set for unplaced scaffolds.

However, there might be more robust general approaches to use in future. It may be that the sequence report linked from the XML assembly record can give an authorative and consistent name and type for some (but not all) seq regions. This would mean a major overhaul of the code for setting metadata though, and would assume that the assembly record is complete and accurate.


New ProteinFeatureType
======================

The error was as follows:
```
2018/04/26 15:09:28 Executing Bio::EnsEMBL::Production::Pipeline::Production::MetaLevels
ParamWarning: value for param('db_type') is used before having been initialized!
Did not insert keys for prediction_transcript, prediction_exon, dna_align_feature, protein_align_feature, repeat_feature.
2018/04/26 15:09:29 Executing Bio::EnsEMBL::Production::Pipeline::Production::PercentGC
Can't call method "adaptor" on an undefined value at /nfs/production/panda/ensemblgenomes/development/dbolser/lib/libensembl-93/ensembl-hive/modules/Bio/EnsEMBL/Hive/Process.pm line 3
```

This is due to a new protein feature type being introduced by InterPro, and that type needed to be added to the `ProteinFeatureType` enum:
https://github.com/Ensembl/ensembl-genomeloader/commit/dd5ca06b3bdf7f60f8a1c80efd7808fa661b8bca

These protein feature types are then attached to the analysis type `cathgene3d`. Note that for display, an entry in `analysis_description` needs to be added to the master table in the production database.

Runnable execution failure
==========================

Error was:
```
2018/04/26 15:09:28 Executing Bio::EnsEMBL::Production::Pipeline::Production::MetaLevels
ParamWarning: value for param('db_type') is used before having been initialized!
Did not insert keys for prediction_transcript, prediction_exon, dna_align_feature, protein_align_feature, repeat_feature.
2018/04/26 15:09:29 Executing Bio::EnsEMBL::Production::Pipeline::Production::PercentGC
Can't call method "adaptor" on an undefined value at /nfs/production/panda/ensemblgenomes/development/dbolser/lib/libensembl-93/ensembl-hive/modules/Bio/EnsEMBL/Hive/Process.pm line 393.
```

There are a set of Runnables in `ensembl-production` which need to be run by GenomeLoader. Rather than running an entire hive pipeline (or even using standalone runner and a registry), the genomeloader code opts to execute the runnables directly. This requires creating dummy Jobs to keep hive code happy. Some Runnables try to interact with the hive database, and even though there is code to try and detect if a hive db is present, recent changes have meant that the hive database belongs to the worker, which doesn't exist in this case, leading to the above error.

To fix this, an empty worker object can be created and used:
https://github.com/Ensembl/ensembl-genomeloader/commit/45151bad2c57e52ea773141f16ebcd3a3b33689c
