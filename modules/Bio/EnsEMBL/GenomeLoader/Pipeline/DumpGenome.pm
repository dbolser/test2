
=head1 LICENSE

Copyright [2009-2015] EMBL-European Bioinformatics Institute

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

=cut

=head1 NAME

Bio::EnsEMBL::GenomeLoader::Pipeline::DumpGenome

=head1 DESCRIPTION

Simple Runnable to dump a genome from ENA

=head1 MAINTAINER

dstaines

=cut

package Bio::EnsEMBL::GenomeLoader::Pipeline::DumpGenome;

use strict;
use warnings;
use base qw/Bio::EnsEMBL::Production::Pipeline::Common::Base/;
use Carp;

sub run {

  my ($self) = @_;

  # accession
  my $accession = $self->param_required('accession');

  # jar
  my $jar = $self->param_required('jar');

  # config file
  my $config_file = $self->param_required('config_file');

  # dump_dir
  my $dump_dir = $self->param('dump_dir') || '.';

  # dump_dir
  my $java_opts = $self->param('java_opts') || '';

  my $dump_file = "$dump_dir/$accession.json";

  $self->log->info("Dumping $accession to $dump_file");

  my $cmd =
    "java $java_opts -jar $jar -c $config_file -s $accession -f $dump_file";
  $self->log->debug("Running $cmd");
  
  system($cmd) == 0 || croak "Could not execute $cmd: $?";

  $self->dataflow_output_id( {
      dump_file => $dump_file
    },
    2 );

  return;
} ## end sub run

1;
