Configuration
=============
The application requires an XML configuration file at the root of the project, for which a template is provided
```
cp genome_materializer/src/main/etc/enagenome_config.xml
```

The following elements are valid:

Database properties
-------------------
* enaUri
** main Oracle Oracle instance
* etaUri
** INSDC assembly Oracle instance
* uniparcUri
** UniParc Oracle instance
* interproUri
** InterPro Oracle instance
* uniProtUri
** UniProt Oracle instance
* rfamUri
** Location of MySQL RFAM database
* enaXmlUrl
** URL for ENA REST service
* maxEnaConnections = 10
** Maximum number of connections to ENA REST
** Default is 10
* lockFileDir 
** Location of directory to use for lock files controlling ENA REST access
** Default is working directory

Model validation properties
---------------------------
* componentSorter
** sorting mechanism to use when loading sequences
** default `automatic`
* allowMissingUpis 
** Whether to allow genes with no UniParc idenifiers
** default `false`
* allowEmptyGenomes 
** Allow genomes with no genes
** default `false`
* minGeneCount 
** Minimum number of genes needed to load the genome
** default 50
* allowMixedCoordSystems
** Allow a mixture of supercontig and chromosome/plasmid coordinate systems
** default `false`

Misc
----
* wgsPolicy 
** Policy when deciding to load WGS or assembled sequences for an assembly
** default `automatic` (uses the CDS count)
* loadAssembly
** Whether to load sequence as contigs represented in INSDC or as toplevel sequences
** default `true`
* useAccessionsForNames 
** Use INSDC assemblies for sequence names rather than attempting to parse them
** default `false`
* loadTrackingReferences = true
** load INSDC tracking references which link Ensembl features to the original portion of the ENA record (can be very long)
** default `true`

ID mapping properties
---------------------
* strictDate (default is 2004-01-01)
** Date after which strict checking is applied to IDs. this reflects historical laxity in ID handling.
* nullCdsTagThreshold 
** Minimum proportion of CDSs with null locus_tags to permit during ID mapping (less than this is deemed an annotation problem, more is historic). Default is set to 0.02
* idUri
** Location of ID database
