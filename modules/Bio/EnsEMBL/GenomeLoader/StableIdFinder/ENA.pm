
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

=cut

# $Source$
# $Revision$
# $Date$
# $Author$
#
# Object for loading data into an ensembl database
#
package Bio::EnsEMBL::GenomeLoader::StableIdFinder::ENA;
use warnings;
use strict;
use Carp;
use Data::Dumper;
use base 'GenomeLoader::StableIdFinder';
use Bio::EnsEMBL::GenomeLoader::Constants
  qw(NAMES XREFS GENE_NAMES BIOTYPES);

sub new {
  my $caller = shift;
  my $class  = ref($caller) || $caller;
  my $self   = $class->SUPER::new(@_);
  return $self;
}

sub _initialize {
  my ( $self, @args ) = @_;
  my %args = @args;
  $self->{species_id} = $args{species_id};
  my $id_search = $args{id_search};
  if ( !$id_search ) {
    $id_search = {
           NAMES()->{GENE} => [ 'get_identifying_id', 'get_public_id' ],
           NAMES()->{TRANSCRIPT} =>
             [ 'get_identifying_id', 'get_public_id' ],
           NAMES()->{TRANSLATION} =>
             [ 'get_identifying_id', 'get_public_id', 'get_parent_id' ],
           NAMES()->{EXON} => [ 'get_parent_id' ] };
  }
  $self->id_search($id_search);
  return;
}
1;

