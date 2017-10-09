
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

Bio::EnsEMBL::GenomeLoader::Pipeline::CreateDatabase

=head1 DESCRIPTION

Simple Runnable to create a multispecies database

=head1 MAINTAINER

dstaines

=cut

package Bio::EnsEMBL::GenomeLoader::Pipeline::CreateDatabase;

use strict;
use warnings;
use base qw/Bio::EnsEMBL::Production::Pipeline::Common::Base/;
use Bio::EnsEMBL::GenomeLoader::SchemaCreator;
use Log::Log4perl qw/get_logger/;

sub run {
  my ($self) = @_;

  if ( $self->param('create') ) {
    
    my $schema = Bio::EnsEMBL::GenomeLoader::SchemaCreator->new(
                                   -PRODUCTION_DBA => $self->production_dba() );

    $self->log()->info( "Creating database " . $self->param('dbname') );

    my $dba = $schema->create_schema( { host   => $self->param('host'),
                                        port   => $self->param('port'),
                                        user   => $self->param('user'),
                                        pass   => $self->param('pass'),
                                        dbname => $self->param('dbname') } );
    for my $genome ( @{ $self->param('load_genomes') } ) {
      $self->$self->dataflow_output_id( { accession  => $genome->{accession},
                                          species_id => $genome->{species_id},
                                          name       => $genome->{name} },
                                        2 );
    }
  }

  for my $genome ( @{ $self->param('delete_genomes') } ) {
    $self->$self->dataflow_output_id( { species_id => $genome->{species_id} },
                                      3 );

  }
  return;
} ## end sub run

1;
