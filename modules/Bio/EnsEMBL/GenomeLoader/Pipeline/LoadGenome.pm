
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

Bio::EnsEMBL::GenomeLoader::Pipeline::FinishDatabase

=head1 DESCRIPTION

Simple Runnable to load a dump file into a multispecies database

=head1 MAINTAINER

dstaines

=cut

package Bio::EnsEMBL::GenomeLoader::Pipeline::LoadGenome;

use strict;
use warnings;
use base qw/Bio::EnsEMBL::Production::Pipeline::Common::Base/;
use Bio::EnsEMBL::GenomeLoader::GenomeLoader;
use JSON;
use File::Slurp qw/read_file/;

sub run {
  my ($self) = @_;
  my $dump_file = $self->param_required('dump_file');
  $self->log()->info( "Parsing genome from " . $dump_file );
  my $genome = decode_json( read_file($dump_file) );

  my $name = $self->param('name');
  if ( defined $name ) {
    $genome->{metaData}{productionName} = $name;
  }

  my $division = $self->param('division');
  if ( defined $name ) {
    $genome->{metaData}{division} = $division;
  }

  my $dba =
    Bio::EnsEMBL::DBSQL::DBAdaptor->new(
                                       -host       => $self->param('host'),
                                       -port       => $self->param('port'),
                                       -user       => $self->param('user'),
                                       -pass       => $self->param('pass'),
                                       -dbname     => $self->param('dbname'),
                                       -species_id => $self->param('species_id')
    );

  $self->log()->info( "Loading genome into " . $dba->dbc()->dbname() );
  my $loader =
    Bio::EnsEMBL::GenomeLoader::GenomeLoader->new(
                                      -DBA            => $dba,
                                      -TAXONOMY_DBA   => $self->taxonomy_dba(),
                                      -PRODUCTION_DBA => $self->production_dba()
    );
  $loader->load_genome($genome);

  return;
} ## end sub run

1;
