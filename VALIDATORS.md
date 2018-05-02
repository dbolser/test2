Overview
========

After processing, the `Genome` instance is passed to an instance of `GenomeValidator` for a series of checks to make sure the model is valid for loading into Ensembl. The main validator is `EnaGenomeValidator` which again delegates to other validators, found in `org.ensembl.genomeloader.materializer.validator`.

In addition, `EnaGenomeValidator` catches a number of specific exceptions from subordinate validators and acts to try to rectify those problems.

Validators
==========
The following validators are run in sequence, raising exceptions if failures are encountered
* `GenomeDescriptionValidator` - Validator that checks the genome description does not match a blacklist. This to to catch genomes that are partial, or from meta genomics or targeted locus projects.
* `GeneCountValidator` - run if allowEmptyGenomes is set to true (see `CONFIG.md`). Checks that the genome contains the minimum number of genes required (see `CONFIG.md`)
* `UniqueComponentNameValidator` - checks for components with duplicate names. Corrected by `EnaGenomeValidator`
* `ComponentNameValidator` - checks that components have valid names (content, length etc.). Corrected by `EnaGenomeValidator`
* `MixedCoordSystemValidator` - run if allowMixedCoordSystems is set to true. This is used to catch issues where a genome contains supercontig as well as chromosome/plasmid systems. Not run by default.
* `ComponentSizeValidator` - Class to check if component sequence length matches the annotated length. 
* `EntityLocationValidator` - Checking if applied location modifiers are valid and locations on linear components are correct. This optionally strips genes with bogus locations from the genome where `skipBrokenLocations` is set to `true`.
* `XrefLengthGenomeValidator` - Checks that xrefs accessions do not exceed 512. Corrected by `EnaGenomeValidator`.
* `AssemblyValidator` - Validator to ensure different levels of an assembly use different coord systems. This is to make sure that levels have been correctly assigned.