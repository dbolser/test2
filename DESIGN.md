# Overview
The two principal components of the GenomeLoader application are the materializer, which retrieves data from a number of sources, principally ENA, and generates a genome model, and the loader, which reads the model and stores it in an Ensembl core database using the Perl API. These components are used by a standalone script to load a single genome, or by the load pipeline to manage genomes within multiple multi-species Ensembl core databases.

# Materializer
The materializer carries out the following processes
* retrieval of metadata from INSDC for a specified assembly. This includes lists of component sequences from ENA
* retrieval and parsing of assembly sequences from the ENA REST interface into a set of model objects
* post-processing of models, which includes restructuring or models and incorporation of additional data from other sources such as UniProt and InterPro
* validation of the final model
* dumping of the models to disk in JSON format

The materializer is written in Java, and can be compiled using `gradle` into an executable "fat" jar containing all required dependencies.

## Materializer entry point
The main entry point is `org.ensembl.genomeloader.materializer.DumpEnaGenome` which accepts an INSDC assembly and (optionally) the file path for the JSON dump. This class retrieves the metadata for the genome, retrieves and parses it from ENA into model classes, processes them and dumps them as JSON. Each step is described below.

Configuration is via an XML file (`enagenome_config.xml`) which is parsed into a bean of the class `org.ensembl.genomeloader.materializer.EnaGenomeConfig`

## Genome model
The classes describing the genome data can be found in `org.ensembl.genomeloader.model` and metadata classes can be found in `org.ensembl.genomeloader.metadata`. In most cases there is an interface and a concrete implementation in the `impl` subpackage, and combination of interfaces such as `Locatable` and `CrossReferenced` are used to support common object properties. 

The principal object model is:
* Genome
** GenomeMetaData
** GenomicComponent (1..n)
*** GenomicComponentMetaData
*** Sequence
*** AssemblyElements (1..n)
*** DatabaseReference (1..n)
*** Gene (1..n)
**** EntityLocation
**** DatabaseReference (1..n)
**** Protein (1..n)
***** EntityLocation
***** DatabaseReference (1..n)
***** Transcript (n..m)
****** EntityLocation
****** DatabaseReference (1..n)
****** Operon (n..m)
*** RnaGene (1..n)
**** EntityLocation
**** DatabaseReference (1..n)
**** Transcript (1..n)
***** DatabaseReference (1..n)
***** EntityLocation

Note that the relationship between Protein and Transcript is an inversion of the usual Ensembl Gene-Transcript-Translation model. This is to support prokaryotic genomes where there may be polycistronic transcripts, though these are not currently found in the ENA models retrieved.

## INDSC assembly identification code
Metadata for a specific INSDC assembly accession is retrieved via the interface `org.ensembl.genomeloader.materializer.genome_collections`. The current implementation, `OracleGenomeCollections`, uses the ENA Oracle instances `ETAPRO` (for assembly data) and `ENAPRO` (for retrieving WGS components). It may be possible in future to use the ENA REST interface for this data but further work is required. 

Metadata is stored in instances of `org.ensembl.genomeloader.metadata.GenomeMetadata` which in turn contains instances of `org.ensembl.genomeloader.metadata.GenomeComponentMetadata` for each ENA entry.

## ENA parsing code
Once metadata has been retrieved, `org.ensembl.genomeloader.materializer.EnaGenomeMaterializer` uses the ENA REST interface to retrieve and parse XML for each ENA entry for the genome in turn. Parsing is carried out using `org.ensembl.genomeloader.materializer.EnaParser`, which uses `nu.xom` to parse the large XML document into elements, which are then passed to different classes in `org.ensembl.genomeloader.materializer.impl` depending on the element being parsed. 

Elements from the feature table are handled by different implementations of `XmlEnaFeatureParser`. A sequential approach is necessary to ensure different elements from the feature table are parsed in the right order. For instance, gene and CDS features need to be parsed before mRNA features. This is specified by the `dependsOn` method of each parser which can be used to build a dependency tree. This is handled by `EnaParser` which sorts parsers by dependency before passing each over the feature table entries in turn.

## Processor and validation code
Once a `Genome` instance has been produced by `EnaGenomeMaterializer`, it is passed to an instance of `GenomeProcessor` for secondary processing. This is currently `EnaGenomeProcessor` which delegates to a series of other processors, found in `org.ensembl.genomeloader.materializer.processors`. These can be divided into two main categories, those which further manipulate the model to handle complex locations, and those which decorate the model with additional data from other sources.

A particularly important processor which straddles both these camps is `AssemblyProcessor` which is essential to retrieve and arrange the entries comprising a CON entry from ENA. This uses the assembly elements parsed out by the main ENA parser to retrieve the sequence components that make up that entry. This is particularly important in larger assemblies with longer sequences.

After processing, the `Genome` instance is passed to an instance of `GenomeValidator` for a series of checks to make sure the model is valid for loading into Ensembl. The main validator is `EnaGenomeValidator` which again delegates to other validators, found in `org.ensembl.genomeloader.materializer.validator`.

## Dump code
Dumping is carried out using the Jackson JSON serialisation library. An entire `Genome` object is dumped to a single file, with a handful of custom serializers for some objects that can be found in `org.ensembl.genomeloader.materializer`. These are used to simplify the data structure for the load process. Note that classes used in the Genome object have been annotated with Jackson-specific annotations to prevent problems with loops caused by 2-way references.

## Auxillary services
The code used is based on part of a much larger framework from the old Integr8 project, and as such contains some generic services and utilites for database interaction, location manipulation etc. These classes can be found in `org.ensembl.genomeloader.util` and `org.ensembl.genomeloader.services`.

# Loader

# Load script

# Load pipeline
## Specification

## Pipeline components
