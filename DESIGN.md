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
    * GenomeMetaData
    * GenomicComponent (1..n)
        * GenomicComponentMetaData
        * Sequence
        * AssemblyElements (1..n)
        * DatabaseReference (1..n)
        * Gene (1..n)
            * EntityLocation
            * DatabaseReference (1..n)
            * Protein (1..n)
                * EntityLocation
                * DatabaseReference (1..n)
                * Transcript (n..m)
                    * EntityLocation
                    * DatabaseReference (1..n)
                    * Operon (n..m)
        * RnaGene (1..n)
            * EntityLocation
            * DatabaseReference (1..n)
            * Transcript (1..n)
                * DatabaseReference (1..n)
                * EntityLocation

Note that the relationship between Protein and Transcript is an inversion of the usual Ensembl Gene-Transcript-Translation model. This is to support prokaryotic genomes where there may be polycistronic transcripts, though these are not currently found in the ENA models retrieved.

## INDSC assembly identification code
Metadata for a specific INSDC assembly accession is retrieved via the interface `org.ensembl.genomeloader.materializer.genome_collections`. The current implementation, `OracleGenomeCollections`, uses the ENA Oracle instances `ETAPRO` (for assembly data) and `ENAPRO` (for retrieving WGS components). It may be possible in future to use the ENA "REST" interface for this data but further work is required. 

Metadata is stored in instances of `org.ensembl.genomeloader.metadata.GenomeMetadata` which in turn contains instances of `org.ensembl.genomeloader.metadata.GenomeComponentMetadata` for each ENA entry.

## ENA parsing code
Once metadata has been retrieved, `org.ensembl.genomeloader.materializer.EnaGenomeMaterializer` uses the ENA "REST" interface to retrieve and parse data for each ENA entry for the genome in turn. Retrieval is carried out by `org.ensembl.genomeloader.materializer.EnaXmlRetriver`, which retrieves data in flatfile format from ENA and parses it into XML using ENA's flatfile tools. A complication here is that ENA no longer serve individual WGS records, forcing us to retrieve and process an entire WGS set. Fortunately this cost is amortised as most load processes will need access to most if not all of a WGS set.

Once data has been retrieved, parsing is carried out using `org.ensembl.genomeloader.materializer.EnaParser`, which uses `nu.xom` to parse the large XML document into elements, which are then passed to different classes in `org.ensembl.genomeloader.materializer.impl` depending on the element being parsed. 

Elements from the feature table are handled by different implementations of `XmlEnaFeatureParser`. A sequential approach is necessary to ensure different elements from the feature table are parsed in the right order. For instance, gene and CDS features need to be parsed before mRNA features. This is specified by the `dependsOn` method of each parser which can be used to build a dependency tree. This is handled by `EnaParser` which sorts parsers by dependency before passing each over the feature table entries in turn.

## Processor and validation code
Once a `Genome` instance has been produced by `EnaGenomeMaterializer`, it is passed to an instance of `GenomeProcessor` for secondary processing. This is currently `EnaGenomeProcessor` which delegates to a series of other processors, found in `org.ensembl.genomeloader.materializer.processors`. These can be divided into two main categories, those which further manipulate the model to handle complex locations, and those which decorate the model with additional data from other sources.

Note that many of these processors use other EBI resources including ENA, InterPro, UniProt and UniParc Oracle instances.

A particularly important processor which straddles both these camps is `AssemblyProcessor` which is essential to retrieve and arrange the entries comprising a CON entry from ENA. This uses the assembly elements parsed out by the main ENA parser to retrieve the sequence components that make up that entry. This is particularly important in larger assemblies with longer sequences.

After processing, the `Genome` instance is passed to an instance of `GenomeValidator` for a series of checks to make sure the model is valid for loading into Ensembl. The main validator is `EnaGenomeValidator` which again delegates to other validators, found in `org.ensembl.genomeloader.materializer.validator`.

## Dump code
Dumping is carried out using the Jackson JSON serialisation library. An entire `Genome` object is dumped to a single file, with a handful of custom serializers for some objects that can be found in `org.ensembl.genomeloader.materializer`. These are used to simplify the data structure for the load process. Note that classes used in the Genome object have been annotated with Jackson-specific annotations to prevent problems with loops caused by 2-way references.

## Auxillary services
The code used is based on parts of a much larger framework from the old Integr8 project, and as such contains some generic services and utilites for database interaction, location manipulation etc. These classes can be found in `org.ensembl.genomeloader.util` and `org.ensembl.genomeloader.services`.

# Loader
The load process uses the Ensembl Perl API to load a MySQL core schema with data retrieved by the Materializer step. There are two main load components, SchemaCreator and GenomeLoader, both written in Perl as they use the Perl API extensively. Otherwise we'd use Java or Python, natch. All the load code has been adapted from the legacy `genomeloader` project, with unnecessary code and general cruft removed.

## SchemaCreator
The code found in `Bio::EnsEMBL::GenomeLoader::SchemaCreator` carries out two main functions:
# creation of an empty MySQL Ensembl core schema and population of controlled tables
# updating of a complete schema to add additional shared data used by all genomes found in that schema

### Creating a schema
The first piece of functionality is invoked by `create_schema`. This creates the database (dropping if needed) and loads DDL from the .sql files found in the core Ensembl repo, It then uses the `ensembl_production` database to populate controlled tables such as `external_db` and `biotype` from master copies of those tables. SchemaCreator returns a `DBAdaptor` instance.  Note that this is set to `species_id` 1, so would need to be changed for accessing other species.

### Finishing a schema
The second piece of functionality is invoked by `finish_schema` and should be run after all genomes have been loaded into the database. This runs the following pieces of database-wide post-processing:
* Cleans up spurious or incorrect versions left in the schema erroneously by the API. In an ideal world we'd fix the API, of course.
* Loads InterPro entries and descriptions using the InterPro Oracle database as a source.
* Cleans up unused analyses from the `analysis` table
* Populates `analysis_description` from the master tables in the `ensembl_production` database.

## GenomeLoader
Individual genomes are loaded into an existing core created as above using the `Bio::EnsEMBL::GenomeLoader::GenomeLoader`. This is invoked by passing a genome hash to `load_genome`. Optionally, `species_id` may also be passed to store genomes in a multispecies database.

The GenomeLoader module uses a succession of other modules in the `Bio::EnsEMBL::GenomeLoader` package to load different parts of the genome. In brief, the following steps are followed:
* Insertion of metadata from the genome hash in the meta table using `GenomeLoader`.
* Creation of the sequence assembly using `ComponentLoader` and `SequenceLoader`. This includes constructing multiple level assemblies using the assembly information stored in the genome hash.p
* For each component, stores fatures using `ComponentLoader` which then uses `GeneLoader` and related modules for storing different kinds of genomic features. Note that each feature store returns a hash representing the genes stored. These are used to generate an iterative hash that can be used to detect changes in the gene models.
* Update of statistics. This invokes a series of Hive modules that are normally run in a production pipeline. These store gene counts, feature densities, calculate pepstats and a'thing.
* Final load of metadata based on data that has been stored.

### Modules used by `GenomeLoader`
* `Bio::EnsEMBL::GenomeLoader::Utils` - rump of old package, reduced now to starting and flushing database transactions
* `Bio::EnsEMBL::GenomeLoader::Constants` - constants and names used throughout the load process
* `Bio::EnsEMBL::GenomeLoader::ObjHashFunctions` - functions used to hash Ensembl objects and produce iterative hashes at the genome level
* `Bio::EnsEMBL::GenomeLoader::AnalysisFinder` - code to map between object types in the ENA derived model and the Ensembl world
* `Bio::EnsEMBL::GenomeLoader::DisplayXrefFinder` - code for determining which names and synonyms to use in Ensembl models, using data retrieved from ENA
* `Bio::EnsEMBL::GenomeLoader::StableIdFinder` - code to determine how to generate a unique stable ID for an Ensembl object
* `Bio::EnsEMBL::GenomeLoader::BaseLoader` - base package from which all other loaders inherit. Contains misc utility methods plus access to shared DBAs.
* `Bio::EnsEMBL::GenomeLoader::ComponentLoader` - code for loading a genome component, including DNA and features. Invokes other loaders below.
* `Bio::EnsEMBL::GenomeLoader::SequenceLoader` - stores an individual seq_region and its DNA
* `Bio::EnsEMBL::GenomeLoader::GeneLoader` -  base package for loading gene features
* `Bio::EnsEMBL::GenomeLoader::GeneLoader::RnaGeneLoader` - base package for loading RNA gene features
* `Bio::EnsEMBL::GenomeLoader::GeneLoader::TRNAGeneLoader` - specific code for handling tRNA
* `Bio::EnsEMBL::GenomeLoader::GeneLoader::RRNAGeneLoader` - specific code for handling rRNA
* `Bio::EnsEMBL::GenomeLoader::GeneLoader::ProteinCodingGeneLoader` - main code for protein coding genes
* `Bio::EnsEMBL::GenomeLoader::FeatureLoader` - base package for loading non-gene features
* `Bio::EnsEMBL::GenomeLoader::FeatureLoader::SimpleFeatureLoader` - code for loading non-repeat features
* `Bio::EnsEMBL::GenomeLoader::FeatureLoader::RepeatFeatureLoader` - code for loading repeat features

# Load script
A simple load script for loading a single genome into a single core database is provided as `modules/load_genome.pl`. This carries out the following steps:
* Creates an empty MySQL Ensembl core schema using SchemaCreator
* Uses the genome-materializer jar to dump the genome from ENA into a JSON file
* Loads the JSON file into memory as a hash
* Loads the genome using GenomeLoader
* Runs finishing steps on the schema using SchemaCreator

# Load pipeline
Currently, there is no pipeline for loading multiple genomes into collection databases, but this would be easily achievable by writing a hive pipeline. Some basic skeletons are provided, the rest is left as an exercise to the reader;
* `Bio::EnsEMBL::GenomeLoader::Pipeline::DumpGenome`
* `Bio::EnsEMBL::GenomeLoader::Pipeline::DeleteGenome`
* `Bio::EnsEMBL::GenomeLoader::Pipeline::GenomeLoaderFactory`
* `Bio::EnsEMBL::GenomeLoader::Pipeline::CreateDatabase`
* `Bio::EnsEMBL::GenomeLoader::Pipeline::FinishDatabase`
* `Bio::EnsEMBL::GenomeLoader::Pipeline::LoadGenome`
