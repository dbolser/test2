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
