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
