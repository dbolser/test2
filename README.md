# Ensembl GenomeLoader

## Overview
This codebase supports the loading of genomes from ENA into Ensembl core databases, either singly or in batch using collections. For more information on design, please see [DESIGN.md].

**Currently this code can only be run from within the EBI due to dependencies on internal databases. This may change in future as alternative services become available**

## Installation
The main GenomeLoader application is written in Perl, but a significant component is written in Java. Perl 5.14 and Java 1.8. Please ensure these are installed on your machine and then follow the steps below.

### Perl dependencies

The following Ensembl repositories (and their individual dependencies) are required:
* [ensembl](https://github.com/Ensembl/ensembl)
* [ensembl-hive](https://github.com/Ensembl/ensembl-hive)
* [ensembl-taxonomy](https://github.com/Ensembl/ensembl-taxonomy)
* [ensembl-production](https://github.com/Ensembl/ensembl-production)
Ensure these are available and referenced by your `PERL5LIB`. 

In addition, you should install the CPAN modules listed in [cpanfile](cpanfile) e.g.
```
cpanm --installdeps .
```

Note that one of the dependencies is [DBD::Oracle](http://search.cpan.org/~pythian/DBD-Oracle-1.74/lib/DBD/Oracle.pm) which requires the installation of Oracle libraries such as those provided via [instantclient](http://www.oracle.com/technetwork/database/features/instant-client/index-097480.html).

### Java dependencies
The Java code used by the application should be compiled into a "fat jar" containing all specified dependencies, as follows:
```
cd genome_materializer
./gradlew fatJar
```
The jar can be found in `./genome_materializer/build/libs/` and will be used from this location by default.

### Other dependencies
Finally, one module used requires the `pepstats` binary from the [EMBOSS](http://emboss.sourceforge.net/) package. Please ensure it is installed and available from your `PATH`.

### Other setup
The application requires an XML configuration file at the root of the project, for which a template is provided
```
cp genome_materializer/src/main/etc/enagenome_config.xml
```
You should edit the config file to contain the database locations and credentials required.

## Loading single genomes

To load a single genome from a specified INSDC assembly accession into a specified core database:
```
perl ./scripts/load_genome.pl -a GCA_000008085 --user ensrw --pass writ3r --host localhost --port 3306 --dbname nanoarchaeum_equitans_core_38_90_1 --division EnsemblFungi --tax_user ensro --tax_host localhost --tax_port 3306 --tax_dbname ncbi_taxonomy  --prod_user ensrw --prod_pass writ3r --prod_host localhost --prod_port 3306 --prod_dbname ensembl_production
```
NB: This assumes the availability of `ensembl_production` and `ncbi_taxonomy` MySQL databases.

For some assemblies, more memory may be required (usually seen as an `java.lang.OutOfMemoryError` from the materialization step). To increase the memory available to Java, please set the environment variable `JAVA_OPTS` before running the script e.g.:
```
export JAVA_OPTS='-Xmx4g'
```

For detailed debugging, please see the files `load_genome.log` and `ena_materializer.log`.

## Loading multiple genomes
Coming soon...
