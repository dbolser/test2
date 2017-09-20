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

my $cli_helper = Bio::EnsEMBL::Utils::CliHelper->new();
my $optsd      = $cli_helper->get_dba_opts();
push( @{$optsd}, "verbose|v" );
push( @{$optsd}, "dump_file|f:s" );

my $usage = sub {
  print
"Usage: $0 --user user [-password pass] --host host --port port --dbname dbname --species_id species_id --dump_file file [--verbose]\n";
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

$log->info( "Parsing genome from " . $opts->{dump_file} );
my $genome = decode_json(read_file($opts->{dump_file}));

#my ($dba) = @{ $cli_helper->get_dbas_for_opts($opts) };

