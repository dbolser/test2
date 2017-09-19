
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

package Bio::EnsEMBL::GenomeLoader::StableIdAssigner::XrefStableIdAssigner;
use warnings;
use strict;
use Carp;
use base 'GenomeLoader::StableIdAssigner::DisplayXrefStableIdAssigner';

sub new {
  my $caller = shift;
  my $class  = ref($caller) || $caller;
  my $self   = $class->SUPER::new(@_);
  if ( !$self->xref_type() ) {
    croak "xref_type not specified";
  }
  return $self;
}

sub xref_type {
  my $self = shift;
  $self->{xref_type} = shift if @_;
  return $self->{xref_type};
}

sub gene_to_id {
  my ( $self, $gene ) = @_;
  my $id = undef;
  for my $xref ( @{ $gene->get_all_DBEntries( $self->xref_type() ) } ) {
    $id = $xref->primary_id();
  }
  return $id;
}

1;
__END__

=head1 NAME

GenomeLoader::StableIdAssigner::XrefStableIdAssigner

=head1 SYNOPSIS

Implementation creating stable ID based on specified xref

=head1 AUTHORS

Dan Staines <dstaines@ebi.ac.uk>

=head1 METHODS

=head2 new
  Title      : new
  Description: Constructor. Invokes GenomeLoader::StableIdAssigner->new as well
  Args       : Hash of arguments. Set multi_transcript to indicate that transcript index should be used in transcript ID
  Returns    : new instance

=head2
  Title      : xref_type
  Description: xref type to use for stable IDs
  Args       : Gene
  Returns    : ID

=head2
  Title      : gene_to_id
  Description: Return ID based on gene xref ID of specified type
  Args       : Gene
  Returns    : ID
