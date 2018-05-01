#!/usr/bin/env perl

=head1 LICENSE

Copyright [2009-2014] EMBL-European Bioinformatics Institute

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
=head1 NAME

load_genome.pl

=head1 SYNOPSIS

Usage: load_genome.pl --user user [--password pass] --host host --port port --dbname dbname [--species_id species_id] [--verbose]
  --division division --accession assembly_setchain [--production_name production_name] [--display_name display_name]
  [--config_file config_file] [--jar jar_file]
  --tax_user user [--tax_password pass] --tax_host host --tax_port port --tax_dbname dbname
  --prod_user user [--prod_password pass] --prod_host host --prod_port port --prod_dbname dbname
  [--interpro_user user [--interpro_password pass] --interpro_host host --interpro_port port --interpro_dbname dbname]

=head1 OPTIONS

=over 4

=item B<--help>

Print a brief help message and exits.

=item B<--verbose>

Print verbose debug messages during processing

=item B<--user>

User for database where core will be created

=item B<--password>

Password for core database server (optional)

=item B<--host>

Core database host name

=item B<--port>

Core database host port

=item B<--dbname>

Name of core database to create

=item B<--species_id>

Species ID to use (default is 1)

=item B<--division>

Ensembl Genomes division name

=item B<--accession>

Set chain of assembly to load (e.g GCA12345 - not the versioned accession!)

=item B<--production_name>

Optional compute-safe name to use during load (default is derived from assembly record)

=item B<--display_name>

Optional human-readable name to use during load (default is derived from assembly record)

=item B<--config_file>

File containing genomeloader configuration (default is etc/enagenome_config.xml)

=item B<--jar>

Fat JAR containing GenomeLoader dump code. Default is genome_materializer/build/libs/genome_materializer-*.jar

=item B<--tax_user>

User for database server containing taxonomy database

=item B<--tax_password>

Password for database server containing taxonomy database

=item B<--tax_host> 

Hostname of database server containing taxonomy database

=item B<--tax_port>

Port of database server containing taxonomy database

=item B<--tax_dbname>

Name of taxonomy database

=item B<--prod_user>

User for database server containing Ensembl production database

=item B<--prod_password>

Password for database server containing Ensembl production database

=item B<--prod_host> 

Hostname of database server containing Ensembl production database

=item B<--prod_port>

Port of database server containing Ensembl production database

=item B<--prod_dbname>

Name of Ensembl production database

=item B<--interpro_user>

User for database server containing Interpro database (default details come from config file)

=item B<--interpro_password>

Password for database server containing Interpro database

=item B<--interpro_host> 

Hostname of database server containing Interpro database

=item B<--interpro_port>

Port of database server containing Interpro database

=item B<--interpro_dbname>

Name of Interpro database

=back

=head1 DESCRIPTION

This script retrieves the data from ENA for a specified assembly, generates a genome model and loads that model into the specified Ensembl core MySQL database

=cut

use strict;
use warnings;

use Log::Log4perl qw/get_logger/;
use Bio::EnsEMBL::Utils::CliHelper;
use JSON;
use File::Slurp qw/read_file/;
use FindBin '$Bin';
use Bio::EnsEMBL::GenomeLoader::GenomeLoader;
use Bio::EnsEMBL::Taxonomy::DBSQL::TaxonomyDBAdaptor;
use Bio::EnsEMBL::DBSQL::DBAdaptor;
use Bio::EnsEMBL::DBSQL::DBConnection;
use Bio::EnsEMBL::GenomeLoader::SchemaCreator;
use File::Temp qw/tempdir/;
use Carp;
use Data::Dumper;

my $cli_helper = Bio::EnsEMBL::Utils::CliHelper->new();
my $optsd = [ @{ $cli_helper->get_dba_opts() },
              @{ $cli_helper->get_dba_opts('tax_') },
              @{ $cli_helper->get_dba_opts('prod_') },
              @{ $cli_helper->get_dba_opts('interpro_') } ];
push( @{$optsd}, "verbose|v" );
push( @{$optsd}, "production_name:s" );
push( @{$optsd}, "display_name:s" );
push( @{$optsd}, "accession|a:s" );
push( @{$optsd}, "jar|j:s" );
push( @{$optsd}, "division:s" );
push( @{$optsd}, "config_file|c:s" );

my $usage = sub {
  print
qq/Usage: load_genome.pl --user user [--password pass] --host host --port port --dbname dbname [--species_id species_id] [--verbose]
  --division division --accession assembly_setchain [--species species_name]
  [--config_file config_file] [--jar jar_file]
  --tax_user user [--tax_password pass] --tax_host host --tax_port port --tax_dbname dbname
  --prod_user user [--prod_password pass] --prod_host host --prod_port port --prod_dbname dbname
  [--interpro_user user [--interpro_password pass] --interpro_host host --interpro_port port --interpro_dbname dbname]
/;
};

my $opts = $cli_helper->process_args( $optsd, $usage );

Log::Log4perl->init("$Bin/log4perl.conf");
my $log = get_logger();
if ( $opts->{verbose} ) {
  $Log::Log4perl::Logger::APPENDER_BY_NAME{'console'}->threshold('DEBUG');
}

if ( !defined $opts->{accession} ) {
  $usage->();
  exit 1;
}

if($opts->{accession} !~ m/^GCA_[0-9]+$/) {
  croak "Accession must be of the form GCA_[0-9]+ with no version";
}

if ( !defined $opts->{division} ) {
  $usage->();
  exit 1;
}

if ( !defined $opts->{config_file} ) {
  $opts->{config_file} = "$Bin/../enagenome_config.xml";
}

if ( !-e $opts->{config_file} ) {
  croak "Config file $opts->{config_file} does not exist";
}

if ( !defined $opts->{interpro_dbname} ) {
  # attempt to parse out details from config file
  my $cfg = read_file( $opts->{config_file} );
  if ( $cfg =~
m/.*<interproUri>.*jdbc:oracle:thin:([^\/]+)\/([^@]+)@\/\/([^:]+):([0-9]+)\/([A-Z0-9]+).*<\/interproUri>.*/
    )
  {
    $opts->{interpro_driver}   = 'Oracle';
    $opts->{interpro_user}     = $1;
    $opts->{interpro_pass}     = $2;
    $opts->{interpro_host}     = $3;
    $opts->{interpro_port}     = $4;
    $opts->{interpro_dbname}   = $5;
  }
}

if ( !defined $opts->{jar} ) {
  my @jars =
    glob "$Bin/../genome_materializer/build/libs/genome_materializer-*.jar";
  if (@jars) {
    $opts->{jar} = $jars[0];
  }
}

if ( !-e $opts->{jar} ) {
  croak
"Could not find genome_materializer jar $opts->{jar} - try running cd genome_materializer && ./gradlew fatJar";
}


my $java_opts = $ENV{JAVA_OPTS} || '';

$opts->{tax_dbname}||='ncbi_taxonomy';
my ($taxonomy_dba_args) =
  @{ $cli_helper->get_dba_args_for_opts( $opts, 1, 'tax_' ) };
my $taxonomy_dba;
if ( defined $taxonomy_dba_args ) {
  $log->info("Connecting to taxonomy database");
  $taxonomy_dba =
    Bio::EnsEMBL::Taxonomy::DBSQL::TaxonomyDBAdaptor->new(%$taxonomy_dba_args);
}

$opts->{prod_dbname}||='ensembl_production';
my ($prod_dba_args) =
  @{ $cli_helper->get_dba_args_for_opts( $opts, 1, 'prod_' ) };
my $prod_dba;
if ( defined $prod_dba_args ) {
  $log->info("Connecting to production database");
  $prod_dba_args->{-species} = 'multi';
  $prod_dba_args->{-group}   = 'production';
  $prod_dba = Bio::EnsEMBL::DBSQL::DBAdaptor->new(%$prod_dba_args);
}

my ($interpro_dba_args) =
  @{ $cli_helper->get_dba_args_for_opts( $opts, 1, 'interpro_' ) };
my $interpro_dbc;
if ( defined $interpro_dba_args ) {
  $log->info("Connecting to Interpro database");
  $interpro_dbc = Bio::EnsEMBL::DBSQL::DBConnection->new(%$interpro_dba_args);
}

my $schema =
  Bio::EnsEMBL::GenomeLoader::SchemaCreator->new(-TAXONOMY_DBA => $taxonomy_dba,
                                                 -PRODUCTION_DBA => $prod_dba,
                                                 -INTERPRO_DBC => $interpro_dbc
  );

my $dba = $schema->create_schema($opts);

my $loader =
  Bio::EnsEMBL::GenomeLoader::GenomeLoader->new( -DBA          => $dba,
                                                 -TAXONOMY_DBA => $taxonomy_dba,
                                                 -PRODUCTION_DBA => $prod_dba );

my $dump_file = tempdir(-CLEANUP=>1) . "/$opts->{accession}.json";

my $cmd =
"java $java_opts -jar $opts->{jar} -c $opts->{config_file} -s $opts->{accession} -f $dump_file";
$log->info("Dumping $opts->{accession} to $dump_file");
$log->debug("Running $cmd");
system($cmd) == 0 || croak "Could not execute $cmd: $?";

$log->info( "Parsing genome from " . $dump_file );
my $genome = decode_json( read_file($dump_file) );

if ( defined $opts->{production_name} ) {
  $log->info("Using production name ".$opts->{production_name});
  $genome->{metaData}{productionName} = $opts->{production_name};
}

if ( defined $opts->{display_name} ) {
  $log->info("Using display name ".$opts->{display_name});
  $genome->{metaData}{name} = $opts->{display_name};
}

$genome->{metaData}{division} = $opts->{division};

$log->info( "Loading genome into schema" );
$loader->load_genome($genome);

$log->info( "Post-processing schema" );
$schema->finish_schema($genome);

unlink $dump_file;
