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

package Bio::EnsEMBL::GenomeLoader::StableIdAssigner::DisplayXrefStableIdAssigner;
use warnings;
use strict;
use Carp;
use base 'GenomeLoader::StableIdAssigner';

sub new {
	my $caller = shift;
	my $class = ref($caller) || $caller;
	my $self = $class->SUPER::new(@_);
	return $self;
}

sub multi_transcript {
	my $self = shift;
	$self->{multi_transcript} = shift if @_;
	return $self->{multi_transcript};
}

sub gene_to_id {
	my ($self,$gene) = @_;
	return $gene->display_xref()->primary_id();
}

sub transcript_to_id {
	my ($self,$transcript, $gene, $index) = @_;
	my $sid;
	if($self->{multi_transcript}) {
		$sid = $gene->stable_id()."-$index";
	} else {
		$sid = $gene->stable_id();
	}
	return $sid;
}

sub translation_to_id {
	my ($self, $translation, $transcript, $gene, $index) = @_;
	return $transcript->stable_id();
}

sub exon_to_id {
	my ($self, $exon, $gene, $index) = @_;
	return $gene->stable_id()."-$index";
}

1;
__END__

=head1 NAME

GenomeLoader::StableIdAssigner::DBIdStableIdAssigner

=head1 SYNOPSIS

Implementation creating stable ID based on display xref of object or its parent.

=head1 AUTHORS

Dan Staines <dstaines@ebi.ac.uk>

=head1 METHODS

=head2 new
  Title      : new
  Description: Constructor. Invokes GenomeLoader::StableIdAssigner->new as well
  Args       : Hash of arguments. Set multi_transcript to indicate that transcript index should be used in transcript ID
  Returns    : new instance

=head2
  Title      : exon_to_id
  Description: Return ID based on gene stable ID plus exon index
  Args       : Exon, gene, exon index
  Returns    : ID

=head2
  Title      : gene_to_id
  Description: Return ID based on gene display xref ID
  Args       : Gene
  Returns    : ID

=head2
  Title      : transcript_to_id
  Description: Return ID based on transcript stable ID, optionally plus transcript index.
  Args       : Transcript, gene, transcript index
  Returns    : ID

=head2
  Title      : translation_to_id
  Description: Return ID based on transcript stable ID
  Args       : Translation, transcript, gene, transcript index
  Returns    : ID
