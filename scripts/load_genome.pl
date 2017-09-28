#!/bin/env perl
# Copyright [2017] EMBL-European Bioinformatics Institute
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# script for loading a single genome into a prepared core database

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
push( @{$optsd}, "accession|a:s" );
push( @{$optsd}, "jar|j:s" );
push( @{$optsd}, "division:s" );
push( @{$optsd}, "config_file|c:s" );

my $usage = sub {
  print
"Usage: $0 --user user [-password pass] --host host --port port --dbname dbname --species_id species_id --division division --dump_file file [--verbose]\n";
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
    $opts->{interpro_password} = $2;
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

my $dump_file = tempdir() . "/$opts->{accession}.json";

my $java_opts = $ENV{JAVA_OPTS} || '';

my $cmd =
"java $java_opts -jar $opts->{jar} -c $opts->{config_file} -s $opts->{accession} -f $dump_file";
$log->info("Dumping $opts->{accession} to $dump_file");
$log->debug("Running $cmd");
system($cmd) == 0 || croak "Could not execute $cmd: $?";

$log->info( "Parsing genome from " . $dump_file );
my $genome = decode_json( read_file($dump_file) );

if ( defined $opts->{species} ) {
  $genome->{metaData}{productionName} = $opts->{species};
}

$genome->{metaData}{division} = $opts->{division};

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
$loader->load_genome($genome);

unlink $dump_file;

$schema->finish_schema($genome);
