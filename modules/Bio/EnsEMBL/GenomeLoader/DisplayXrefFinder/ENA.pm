
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
package Bio::EnsEMBL::GenomeLoader::DisplayXrefFinder::ENA;
use warnings;
use strict;
use Carp;
use Data::Dumper;
use Bio::EnsEMBL::GenomeLoader::Constants
  qw(NAMES XREFS GENE_NAMES BIOTYPES);
use base 'GenomeLoader::DisplayXrefFinder';

sub new {
  my $caller = shift;
  my $class  = ref($caller) || $caller;
  my $self   = $class->SUPER::new(@_);
  return $self;
}

sub _initialize {
  my ( $self, @args ) = @_;
  $self->SUPER::_initialize(@args);
  my $xrefs_search = {
    NAMES()->{GENE} =>
      [ 'get_display_xref_for_gene', 'get_display_xref_for_gene_name' ],
    NAMES()->{TRANSCRIPT} =>
      [ 'get_hybrid_display_xref_protein_id_for_transcript' ] };
  $self->display_xrefs_search($xrefs_search);
  return;
}

1;
__END__

=head1 NAME

GenomeLoader::DisplayXrefFinder::ENA

=head1 SYNOPSIS

Module providing standard functionality for determining an xref using the default set of search methods. For Gene:
				'get_display_xref_for_gene',
				'get_display_xref_for_gene_name',
				'get_display_xref_for_gene_locus',
				'get_display_xref_protein_id_for_gene'
For Transcript:
				'get_display_xref_for_transcript_name',
				'get_display_xref_for_uniprot_swissprot',
				'get_display_xref_for_uniprot_trembl'

=head1 AUTHORS

Dan Staines <dstaines@ebi.ac.uk>

=head1 METHODS

=head2 new
  Title      : new
  Description: constructor
  Args       : hash of arguments incl. division
  Returns	 : new instance
