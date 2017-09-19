
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

package Bio::EnsEMBL::GenomeLoader::StableIdAssigner;
use warnings;
use strict;
use Carp;

sub new {
  my ( $caller, @args ) = @_;
  my $class = ref($caller) || $caller;
  my $self = {@args};
  bless( $self, $class );
  return $self;
}

sub dba {
  my $self = shift;
  $self->{dba} = shift if @_;
  return $self->{dba};
}

sub gene_to_id {
  my ( $self, $gene ) = @_;
  croak("gene_to_id not implemented!");
}

sub transcript_to_id {
  my ( $self, $transcript, $gene, $index ) = @_;
  croak("transcript_to_id not implemented!");
}

sub translation_to_id {
  my ( $self, $translation, $transcript, $gene, $index ) = @_;
  croak("translation_to_id not implemented!");
}

sub exon_to_id {
  my ( $self, $exon, $gene, $index ) = @_;
  croak("exon_to_id not implemented!");
}
1;
__END__

=head1 NAME

GenomeLoader::StableIdAssigner

=head1 SYNOPSIS

Base module to assign stable IDs for supplied ensembl objects.

=head1 AUTHORS

Dan Staines <dstaines@ebi.ac.uk>

=head1 METHODS

=head2 new
  Title      : new
  Description: Constructor.
  Args       : Hash of arguments incl. dba
  Returns    : new instance

=head2 dba
  Title      : dba
  Description: Get/set database adaptor
  Args       : Database adaptor
  Returns    : Database adaptor

=head2
  Title      : exon_to_id
  Description: Stub for getting ID for an exon
  Args       : Exon, gene, exon index
  Returns    : ID

=head2
  Title      : gene_to_id
  Description: Stub for getting ID for a gene
  Args       : Gene
  Returns    : ID

=head2
  Title      : transcript_to_id
  Description: Stub for getting ID for a transcript
  Args       : Transcript, gene, transcript index
  Returns    : ID

=head2
  Title      : translation_to_id
  Description: Stub for getting ID for a transcript
  Args       : Translation, transcript, gene, transcript index
  Returns    : ID
