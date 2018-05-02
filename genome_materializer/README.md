Overview
========
This Java project contains code needed to materialize a specified INSDC assembly as JSON. It is used by the [ensembl-genomeloader](https://github.com/Ensembl/ensembl-genomeloader) project.

Compilation
===========
To build a jar file containing all :
```
./gradlew fatJar
```
This should create a jar e.g. `build/libs/genome_materializer-1.0.jar`

Execution
=========
The jar can be executed to dump a specified assembly as JSON e.g: 
```
java -jar build/libs/genome_materializer-1.0.jar -s GCA_000008085
```

To specify an output file:
```
java -jar build/libs/genome_materializer-1.0.jar -s GCA_000008085 -f /path/to/mygenome.json
```

Note that by default, an XML config file is expected at `./etc/ena_genomeconfig.xml`. `src/main/examples/ena_genomeconfig.xml` contains an example.
To specify a different file, use:
```
java -jar build/libs/genome_materializer-1.0.jar -s GCA_000008085 -c /path/to/ena_genomeconfig.xml
```

Important: You _cannot_ use a versioned accession such as `GCA_000002725.2` - just the unversioned set chain (`GCA_000002725`)


