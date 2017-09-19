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

package Bio::EnsEMBL::GenomeLoader::StableIdAssigner::DbIdStableIdAssigner;
use base 'GenomeLoader::StableIdAssigner';
use warnings;
use strict;
use Carp;

sub new {
	my $caller = shift;
	my $class = ref($caller) || $caller;
	my $self = $class->SUPER::new(@_);
	return $self;
}


sub id_root {
	my $self = shift;
	$self->{id_root} = shift if @_;
	return $self->{id_root};
}

my %type_to_string= (
	'Bio::EnsEMBL::Gene'=>'G',
	'Bio::EnsEMBL::Transcript'=>'T',
	'Bio::EnsEMBL::Exon'=>'E',
	'Bio::EnsEMBL::Translation'=>'P'
);

my $pad_len = 11;

sub obj_to_id {
	my ($self,$obj) = @_;
	my $type = $type_to_string{ref($obj)};
	croak("Unsupported type ".ref($obj)) unless $type;
	return sprintf("%s%s%0${pad_len}d", $self->id_root,$type,$obj->dbID());
}

sub gene_to_id {
	my($self,$gene) = @_;
	return $self->obj_to_id($gene);
}

sub transcript_to_id {
	my($self,$transcript,$gene,$index) = @_;
	return $self->obj_to_id($transcript);
}

sub translation_to_id {
	my($self,$translation,$transcript,$gene,$index) = @_;
	return $self->obj_to_id($translation);
}

sub exon_to_id {
	my($self,$exon,$gene,$index) = @_;
	return $self->obj_to_id($exon);
}

1;
__END__

=head1 NAME

GenomeLoader::StableIdAssigner::DBIdStableIdAssigner

=head1 SYNOPSIS

Implementation creating stable ID based on dbID of ensembl object

=head1 AUTHORS

Dan Staines <dstaines@ebi.ac.uk>

=head1 METHODS

=head2 new
  Title      : new
  Description: Constructor. Invokes GenomeLoader::StableIdAssigner->new as well
  Args       : Hash of arguments
  Returns    : new instance

=head2
  Title      : exon_to_id
  Description: Return ID based on exon->dbID
  Args       : Exon, gene, exon index
  Returns    : ID

=head2
  Title      : gene_to_id
  Description: Return ID based on gene->dbID
  Args       : Gene
  Returns    : ID

=head2
  Title      : transcript_to_id
  Description: Return ID based on transcript->dbID
  Args       : Transcript, gene, transcript index
  Returns    : ID

=head2
  Title      : translation_to_id
  Description: Return ID based on translation->dbID
  Args       : Translation, transcript, gene, transcript index
  Returns    : ID
