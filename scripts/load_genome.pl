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
use Data::Dumper;

my $cli_helper = Bio::EnsEMBL::Utils::CliHelper->new();
my $optsd = [ @{ $cli_helper->get_dba_opts() },
              @{ $cli_helper->get_dba_opts('tax_') },
              @{ $cli_helper->get_dba_opts('prod_') } ];
push( @{$optsd}, "verbose|v" );
push( @{$optsd}, "dump_file|f:s" );
push( @{$optsd}, "genebuild:s" );
push( @{$optsd}, "division:s" );

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

if ( !defined $opts->{dump_file} ) {
  $usage->();
  exit 1;
}

if ( !defined $opts->{division} ) {
  $usage->();
  exit 1;
}

$log->info( "Parsing genome from " . $opts->{dump_file} );
my $genome = decode_json( read_file( $opts->{dump_file} ) );

if ( !defined $opts->{species} ) {
  $opts->{species} = lc $genome->{metaData}{name};
  $opts->{species} =~ s/[^A-z0-9_]+/_/g;
}
if ( !defined $opts->{genebuild} ) {
  $opts->{genebuild} = $genome->{metaData}{updateDate};
  $opts->{genebuild} =~ s/[0-9]+$/ENA/;
}

$genome->{metaData}{productionName} = $opts->{species};
$genome->{metaData}{genebuild}      = $opts->{genebuild};
$genome->{metaData}{division}       = $opts->{division};
$genome->{metaData}{provider} ||= 'European Nucleotide Archive';
$genome->{metaData}{providerUrl} ||=
  'http://www.ebi.ac.uk/ena/data/view/' . $genome->{metaData}{id};

my ($dba) = @{ $cli_helper->get_dbas_for_opts($opts) };
my ($taxonomy_dba_args) =
  @{ $cli_helper->get_dba_args_for_opts( $opts, 1, 'tax_' ) };
my $taxonomy_dba;
if ( defined $taxonomy_dba_args ) {
  $log->info("Connecting to taxonomy database");
  $taxonomy_dba =
    Bio::EnsEMBL::Taxonomy::DBSQL::TaxonomyDBAdaptor->new(%$taxonomy_dba_args);
}
my ($prod_dba_args) =
  @{ $cli_helper->get_dba_args_for_opts( $opts, 1, 'prod_' ) };
my $prod_dba;
if ( defined $prod_dba_args ) {
  $log->info("Connecting to production database");
  $prod_dba_args->{-species} = 'multi';
  $prod_dba_args->{-group} = 'production';
  $prod_dba =  
    Bio::EnsEMBL::DBSQL::DBAdaptor->new(%$prod_dba_args);
}
my $loader =
  Bio::EnsEMBL::GenomeLoader::GenomeLoader->new(-DBA          => $dba,
                                                -TAXONOMY_DBA => $taxonomy_dba,
                                                -PRODUCTION_DBA => $prod_dba
  );
$loader->load_genome($genome);
