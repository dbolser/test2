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
# Base object for returning a display_xref for a given object based on rules
#
package Bio::EnsEMBL::GenomeLoader::StableIdFinder;
use base 'Bio::Root::Root';
use warnings;
use strict;
use Carp;
use Data::Dumper;
use GenomeLoader::Constants qw(NAMES XREFS GENE_NAMES BIOTYPES);
use Log::Log4perl qw(get_logger);

sub new {
  my ($caller, @args) = @_;
  my $class = ref($caller) || $caller;
  my $self;
  if ($class =~ /GenomeLoader::StableIdFinder::(\S+)/x) {
	$self = $class->SUPER::new(@args);
	$self->_initialize(@args);
  } else {
	my %args = @args;
	$self = {};
	bless($self, $class);
	$self->_initialize(@args);
	$self->{species_id} = $args{dba}->species_id();
  }
  if (!$self->log()) {
	$self->log(get_logger());
  }
  return $self;
}

sub _initialize {
  my ($self, @args) = @_;
  my %args = @args;
  $self->{species_id} = $args{species_id};
  my $id_search = $args{id_search};
  if (!$id_search) {
	$id_search = {
	  NAMES()->{GENE}        => ['get_identifying_id', 'get_public_id'],
	  NAMES()->{TRANSCRIPT}  => ['get_identifying_id', 'get_public_id', 'get_parent_id'],
	  NAMES()->{TRANSLATION} => ['get_identifying_id', 'get_public_id', 'get_parent_id'],
	  NAMES()->{EXON} => ['get_parent_id']};
  }
  $self->id_search($id_search);
  return;
}

sub log {
  my $self = shift;
  $self->{log} = shift if @_;
  return $self->{log};
}

sub id_search {
  my $self = shift;
  $self->{id_search} = shift if @_;
  return $self->{id_search};
}

sub get_id_search_for_obj {
  my ($self, $eobj) = @_;
  return $self->id_search()->{ref($eobj)};
}

sub get_stable_id {
  my ($self, $iobj, $eobj, $parent, $index) = @_;
  if (!$iobj) {
	return;
  }
  my $search = $self->get_id_search_for_obj($eobj);
  if (!$search) {
	return;
  }
  my $stable_id;
  foreach my $id_source (@{$search}) {
	$stable_id = $self->$id_source($iobj, $eobj, $parent, $index);
	last if $stable_id;
  }
  if (!$stable_id) {
	$self->log()->warn("Cannot get stable ID for object of type " . ref($eobj) . " using methods " . join(',', @$search));
  }
  return $stable_id;
}

sub get_identifying_id {
  my ($self, $iobj) = @_;
  return $iobj->{identifyingId};
}

sub get_public_id {
  my ($self, $iobj) = @_;
  return $iobj->{publicId};
}

sub get_igi_id {
  my ($self, $iobj) = @_;
  return $iobj->{igi};
}

sub get_persistable_id {
  my ($self, $iobj) = @_;
  return $iobj->{persistableId};
}

sub get_parent_id {
  my ($self, $iobj, $eobj, $parent, $index) = @_;
  if (defined $parent && defined $parent->stable_id()) {
	return $parent->stable_id() . "-$index";
  }
  return;
}

1;
__END__

=head1 NAME

GenomeLoader::DisplayXrefFinder

=head1 SYNOPSIS

Base module to determine the most suitable xref to use for display for an ensembl gene. Uses hash of methods to call in turn for different object, retrieved by display_xrefs_search.

=head1 AUTHORS

Dan Staines <dstaines@ebi.ac.uk>

=head1 METHODS

=head2 new
  Title      : new
  Description: base constructor
  Args       : hash of arguments incl. division
  Returns	 : new instance

=head2 _initialize
  Title      : _initialize
  Description: internal constructor helper
  Args       : set up the list of methods to consult for an object

=head2 get_display_xref
  Title      : get_display_xref
  Description: general method for getting a display xref for a supplied object. Delegates to internal methods retrieved by display_xrefs_search to find an xref. Note that parents are also supplied for transcripts (optionally).
  Args       : object hash, ensembl target object, parent object (optional), child index (optional).
  Returns    : Bio::Ensembl::DBEntry

=head2 add_synonyms
  Title      : add_synonyms
  Description: map gene name synonyms onto an xref
  Args       : hash, target DBEntry

=head2 display_xrefs_search
  Title      : display_xrefs_search
  Description: get hash of lists of method references to use for finding xrefs
  Returns    : hash of lists of method references

=head2 get_db_display_xref
  Title      : get_db_display_xref
  Description: Find the first DBEntry attached to the target object with the specified dbname
  Args       : object hash, ensembl target object, dbname.
  Returns    : Bio::Ensembl::DBEntry

=head2 get_default_display_xref
  Title      : get_default_display_xref
  Description: Create a dummy xref based on the biotype and stable ID separated by a pipe
  Args       : object hash, ensembl target object
  Returns    : Bio::Ensembl::DBEntry

=head2 get_display_xref_for_gene
  Title      : get_display_xref_for_gene
  Description: Get an xref for the supplied gene based on the name from the gene hash
  Args       : object hash, ensembl target object
  Returns    : Bio::Ensembl::DBEntry

=head2 get_display_xref_for_gene_locus
  Title      : get_display_xref_for_gene_locus
  Description:  Get the xref for the supplied gene that contains the locus tag
  Args       : object hash, ensembl target object
  Returns    : Bio::Ensembl::DBEntry

=head2 get_display_xref_for_gene_name
  Title      : get_display_xref_for_gene_name
  Description: Get the xref for the supplied gene that contains the locus name
  Args       : object hash, ensembl target object
  Returns    : Bio::Ensembl::DBEntry

=head2 get_display_xref_for_transcript_name
  Title      : get_display_xref_for_transcript_name
  Description: Get an xref for the supplied transcript based on the name from the transcript hash
  Args       : object hash, ensembl target object
  Returns    : Bio::Ensembl::DBEntry

=head2 get_display_xref_for_uniprot_trembl
  Title      : get_display_xref_for_uniprot_trembl
  Description: Find the first DBEntry attached to the target object that matches UniProtKB/trembl
  Args       : object hash, ensembl target object
  Returns    : Bio::Ensembl::DBEntry

=head2 get_display_xref_for_uniprot_swissprot
  Title      : get_display_xref_for_uniprot_swissprot
  Description: Find the first DBEntry attached to the target object that matches UniProtKB/SwissProt
  Args       : object hash, ensembl target object
  Returns    : Bio::Ensembl::DBEntry

=head2 get_display_xref_protein_id_for_gene
  Title      : get_display_xref_protein_id_for_gene
  Description: Find the first DBEntry attached to the canonical transcript for a given gene that contains the protein_id
  Args       : object hash, ensembl target object
  Returns    : Bio::Ensembl::DBEntry

=head2 get_display_xrefs_search_for_obj
  Title      : get_display_xrefs_search_for_obj
  Description: Get the list of search methods appropriate to the supplied object
  Args       : ensembl target object
  Returns    : list of function references

=head2 get_feature_display_xref
  Title      : get_feature_display_xref
  Description: Create an xref based on the contents of the supplied attribute
  Args       : object hash, target object, database name, attribute name
  Returns    : Bio::Ensembl::DBEntry

=head2 get_feature_display_xref_by_name
  Title      : get_feature_display_xref_by_name
  Description: Create an xref based on the supplied ID and database name
  Args       : target object, database name, primary ID, display ID
  Returns    : Bio::Ensembl::DBEntry

=head2 get_gene_names_display_xref
  Title      : get_feature_display_xref_by_name
  Description: Create an xref for a gene given the specified name type
  Args       : object hash, target object, database name, attribute name
  Returns    : Bio::Ensembl::DBEntry

=head2 get_stable_id
  Title      : get_stable_id
  Description: Get the stable ID type for the specified object
  Args       : target object
  Returns    : Bio::Ensembl::DBEntry

=head2 get_primary_id
  Title      : get_primary_id
  Description: Create a dummy ID for an object based on biotype and ID
  Args       : target object
  Returns    : Bio::Ensembl::DBEntry

=head2 get_xref_type_for_gene
  Title      : get_xref_type_for_gene
  Description: Get the correct DB name for a given gene
  Args       : target object
  Returns    : string

=head2 get_xref_type_for_transcript
  Title      : get_xref_type_for_transcript
  Description: Get the correct DB name for a given transcript
  Args       : target object
  Returns    : string

=head2 transcript_to_pid
  Title      : transcript_to_pid
  Description: Get the protein ID for a transcript
  Args       : target transcript
  Returns    : Bio::Ensembl::DBEntry
