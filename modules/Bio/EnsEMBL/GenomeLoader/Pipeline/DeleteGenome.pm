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

Bio::EnsEMBL::GenomeLoader::Pipeline::DeleteGenome

=head1 DESCRIPTION

Simple Runnable to delete a genome from a multispecies database

=head1 MAINTAINER

dstaines

=cut
package Bio::EnsEMBL::GenomeLoader::Pipeline::DeleteGenome; 

use strict;
use warnings;
use base qw/Bio::EnsEMBL::Production::Pipeline::Common::Base/;
use Bio::EnsEMBL::Production::Utils::GenomeCopier;
use  Bio::EnsEMBL::DBSQL::DBAdaptor;
sub run {
    my ($self) = @_;
      my $dba =
    Bio::EnsEMBL::DBSQL::DBAdaptor->new( -host   => $self->param_required('host'),
                                         -port   => $self->param('port'),
                                         -user   => $self->param_required('user'),
                                         -pass   => $self->param('pass'),
                                         -dbname => $self->param_required('dbname'),
                                         -species_id=>$self->param_required('species_id') );
    
    $self->log()->info("Deleting genome ".$dba->species_id()." from ".$dba->dbc()->dbname());
    Bio::EnsEMBL::Production::Utils::GenomeCopier->new()->delete($dba);
    return;
}

1;