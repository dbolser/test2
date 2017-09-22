
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
# uses dynamic loading to load division specific implementations e.g. EnsemblProtists
#
package Bio::EnsEMBL::GenomeLoader::DisplayXrefFinder;
use base 'Bio::Root::Root';
use warnings;
use strict;
use Carp;
use Data::Dumper;
use Bio::EnsEMBL::GenomeLoader::Constants qw/NAMES XREFS GENE_NAMES BIOTYPES/;
use Bio::EnsEMBL::GenomeLoader::AnalysisFinder qw/get_analysis_by_name/;
use Bio::EnsEMBL::Utils::Argument qw/rearrange/;

sub new {
  my ( $caller, @args ) = @_;
  my $class = ref($caller) || $caller;

  my $self = bless( {}, $class );

  ( $self->{dba} ) = rearrange( ['DBA'], @args );
  
  $self->{species_id} = $self->{dba}->species_id();

  $self->{gl_name_xref} = get_analysis_by_name('gl_name_xref');

  my $xrefs_search = {
    NAMES()->{GENE} =>
    ['get_display_xref_for_gene', 'get_display_xref_for_gene_name']
    ,
    NAMES()->{TRANSCRIPT} => [
                 'get_display_xref_for_transcript_gene',
                 'get_display_xref_for_uniprot_swissprot',
                 'get_display_xref_for_uniprot_trembl']};

  $self->description_sources(
               {db_name  => XREFS()->{UNIPROT_SWISSPROT},
                db_label => 'UniProtKB/Swiss-Prot'},
               {db_name  => XREFS()->{UNIPROT_TREMBL},
                db_label => 'UniProtKB/TrEMBL'});
  $self->display_xrefs_search($xrefs_search);
  my $embl_gene_anal_2_xref = {
           ena       => XREFS()->{EMBL_GENE_NAME},
           embl_bank => XREFS()->{EMBL_GENE_NAME},
           ensembl_bacteria_alignment => XREFS()->{EB_GENE_NAME}
  };
  $self->{gene_anal_2_xref} = {
     BIOTYPES()->{PROTEIN_CODING_GENE_TYPE} => $embl_gene_anal_2_xref,
     BIOTYPES()->{PSEUDOGENE_TYPE}          => $embl_gene_anal_2_xref,
     BIOTYPES()->{NON_TRANSLATING_TYPE}     => $embl_gene_anal_2_xref,
     BIOTYPES()->{NCRNA_TYPE}               => XREFS()->{RFAM},
     BIOTYPES()->{MISC_RNA_TYPE}            => XREFS()->{RFAM},
     BIOTYPES()->{ANTISENSE_TYPE}           => XREFS()->{RFAM},
     BIOTYPES()->{RNASEP_TYPE}              => XREFS()->{RFAM},
     BIOTYPES()->{TMRNA_TYPE}               => XREFS()->{RFAM},
     BIOTYPES()->{SRP_RNA_TYPE}             => XREFS()->{RFAM},
     BIOTYPES()->{SNO_RNA_TYPE}             => XREFS()->{RFAM},
     BIOTYPES()->{SN_RNA_TYPE}              => XREFS()->{RFAM},
     BIOTYPES()->{MIRNA_TYPE}               => XREFS()->{RFAM},
     BIOTYPES()->{RRNA_TYPE}                => XREFS()->{RNAMMER},
     BIOTYPES()->{TRNA_TYPE}                => XREFS()->{TRNASCAN},
     BIOTYPES()->{TRNA_PSEUDO_TYPE}         => XREFS()->{TRNASCAN}};
  my $embl_transcript_anal_2_xref = {
       ena       => XREFS()->{EMBL_TRANSCRIPT_NAME},
       embl_bank => XREFS()->{EMBL_TRANSCRIPT_NAME},
       ensembl_bacteria_alignment => XREFS()->{EB_TRANSCRIPT_NAME}
  };
  $self->{transcript_anal_2_xref} = {
   BIOTYPES()->{PROTEIN_CODING_GENE_TYPE} =>
     $embl_transcript_anal_2_xref,
   BIOTYPES()->{PSEUDOGENE_TYPE}      => $embl_transcript_anal_2_xref,
   BIOTYPES()->{NON_TRANSLATING_TYPE} => $embl_transcript_anal_2_xref,
   BIOTYPES()->{NCRNA_TYPE}           => XREFS()->{RFAM},
   BIOTYPES()->{MISC_RNA_TYPE}        => XREFS()->{RFAM},
   BIOTYPES()->{ANTISENSE_TYPE}       => XREFS()->{RFAM},
   BIOTYPES()->{RNASEP_TYPE}          => XREFS()->{RFAM},
   BIOTYPES()->{TMRNA_TYPE}           => XREFS()->{RFAM},
   BIOTYPES()->{MIRNA_TYPE}           => XREFS()->{RFAM},
   BIOTYPES()->{SRP_RNA_TYPE}         => XREFS()->{RFAM},
   BIOTYPES()->{SNO_RNA_TYPE}         => XREFS()->{RFAM},
   BIOTYPES()->{SN_RNA_TYPE}          => XREFS()->{RFAM},
   BIOTYPES()->{RRNA_TYPE}            => XREFS()->{RNAMMER},
   BIOTYPES()->{TRNA_TYPE}            => XREFS()->{TRNASCAN},
   BIOTYPES()->{TRNA_PSEUDO_TYPE}     => XREFS()->{TRNASCAN}};
  return $self;
} ## end sub _initialize

sub get_description_source {
  my ($self, $egene, $igene) = @_;
  my $des = '';
  for my $des_name ($self->description_sources()) {
  my $dbname;
  my $acc;
  for my $xref (
    grep {
    $des_name->{db_name} eq $_->{databaseReferenceType}{ensemblName}
    } @{$igene->{xrefs}})
  {
    $dbname = $des_name->{db_label};
    $acc    = $xref->{primaryIdentifier};
    last;
  }
  if ($dbname && $acc) {
    $des = ' [Source:' . $dbname . ';Acc:' . $acc . ']';
    last;
  }
  }
  return $des;
}

sub description_sources {
  my $self = shift;
  $self->{description_sources} = [@_] if @_;
  return @{$self->{description_sources}};
}

sub display_xrefs_search {
  my $self = shift;
  $self->{display_xrefs_search} = shift if @_;
  return $self->{display_xrefs_search};
}

sub get_display_xrefs_search_for_obj {
  my ($self, $eobj) = @_;
  return $self->display_xrefs_search()->{ref($eobj)};
}

sub get_display_xref {
  my ($self, $iobj, $eobj, $eobj_parent, $index) = @_;
  if (!$iobj) {
  return;
  }
  my $search = $self->get_display_xrefs_search_for_obj($eobj);
  if (!$search) {
  return;
  }
  my $display_xref;
  foreach my $display_xref_source (@{$search}) {
  $display_xref =
    $self->$display_xref_source($iobj, $eobj, $eobj_parent, $index);
  last if $display_xref;
  }
  if (!$display_xref) {
  $display_xref =
    $self->get_default_display_xref($iobj, $eobj, $eobj_parent,
                    $index);
  }
  return $display_xref;
}

sub get_display_xref_for_gene_locus {
  my ($self, $iobj, $eobj) = @_;
  return
  $self->get_gene_names_display_xref(
  $iobj, $eobj,
  $self->get_xref_type_for_gene($iobj, $eobj),
  GENE_NAMES()->{ORDEREDLOCUSNAMES});
}

sub get_display_xref_for_gene_name {
  my ($self, $iobj, $eobj) = @_;
  return
  $self->get_gene_names_display_xref(
  $iobj, $eobj,
  $self->get_xref_type_for_gene($iobj, $eobj),
  GENE_NAMES()->{NAME});
}

sub get_display_xref_for_gene {
  my ($self, $iobj, $eobj) = @_;
  my $name       = $iobj->{NAMES()->{GENE_ATTRIB_NAME}};
  my $display_id = $name;
  if ($self->{species_id}) {
  $name .= '-' . $self->{species_id};
  }
  my $display_xref =
  $self->get_feature_display_xref_by_name($eobj,
              $self->get_xref_type_for_gene($iobj, $eobj),
              $name, $display_id);
  if (defined $display_xref) {
  $self->add_synonyms($display_xref, $iobj);
  }
  return $display_xref;
}

sub get_display_xref_protein_id_for_gene {
  my ($self, $iobj, $eobj) = @_;
  my $display_xref;
  if ($eobj->canonical_transcript()) {
  $display_xref =
    $self->transcript_to_pid($eobj->canonical_transcript());
  }
  if ($eobj->get_all_Transcripts()) {
  for my $transcript (@{$eobj->get_all_Transcripts()}) {
    if ($display_xref) {
    last;
    }
    $display_xref = $self->transcript_to_pid($transcript);
  }
  }
  return $display_xref;
}

sub get_hybrid_display_xref_protein_id_for_transcript {
  my ($self, $iobj, $eobj, $gene) = @_;
  my $display_xref = $self->transcript_to_pid($eobj);
  if (defined $display_xref && defined $eobj->stable_id() && defined $gene->external_name()) {
  my $idStr =
    $gene->external_name() . '/' . $display_xref->primary_id();
  $display_xref =
    Bio::EnsEMBL::DBEntry->new(
               -DBNAME => XREFS()->{'EMBL_TRANSCRIPT_NAME'},
               -PRIMARY_ID => $idStr,
               -DISPLAY_ID => $idStr,
               -ANALYSIS   => $self->{gl_name_xref});
  $eobj->add_DBEntry($display_xref);
  }
  return $display_xref;
}

sub get_display_xref_protein_id_for_transcript {
  my ($self, $iobj, $eobj) = @_;
  return $self->transcript_to_pid($eobj);
}

sub transcript_to_pid {
  my ($self, $transcript) = @_;
  my $display_xref;
  for my $xref (@{$transcript->get_all_DBEntries()}) {
  if ($xref->dbname() eq XREFS()->{PROTEIN_ID}) {
    $display_xref = $xref;
    last;
  }
  }
  if (!$display_xref) {
  my $translation = $transcript->translation();
  if ($translation) {
    for my $xref (@{$translation->get_all_DBEntries()}) {
    if ($xref->dbname() eq XREFS()->{PROTEIN_ID}) {
      $display_xref = $xref;
      last;
    }
    }
  }
  }
  return $display_xref;
}

sub get_display_xref_for_transcript_name {
  my ($self, $iobj, $eobj) = @_;

  my $display_id = $self->get_primary_id($eobj);
  return
  $self->get_feature_display_xref_by_name($eobj,
            $self->get_xref_type_for_transcript($iobj, $eobj),
            $display_id, $display_id);
}

sub get_display_xref_for_transcript_gene {
  my ($self, $iobj, $transcript, $gene, $index) = @_;
  confess "Gene not supplied"  unless defined $gene;
  confess "Index not supplied" unless defined $index;
  my $sid = $gene->display_xref();
  my $new_xref;
  if ($sid) {
  my $pid  = $sid->primary_id() . "-$index";
  my $pids = $sid->display_id() . "-$index";
  my $dbname =
    $self->get_xref_type_for_transcript($iobj, $transcript);
  $new_xref =
    Bio::EnsEMBL::DBEntry->new(-DBNAME     => $dbname,
                 -PRIMARY_ID => $pid,
                 -DISPLAY_ID => $pids,
                 -ANALYSIS   => $self->{gl_name_xref});
  $transcript->add_DBEntry($new_xref);
  }
  return $new_xref;
}

sub get_display_xref_for_uniprot_swissprot {
  my ($self, $iobj, $eobj) = @_;
  return
  $self->get_db_display_xref($iobj, $eobj,
                 XREFS()->{UNIPROT_SWISSPROT});
}

sub get_display_xref_for_uniprot_trembl {
  my ($self, $iobj, $eobj) = @_;
  return $self->get_db_display_xref($iobj, $eobj,
                  XREFS()->{UNIPROT_TREMBL});
}

sub get_default_display_xref {
  my ($self, $iobj, $eobj) = @_;

  # Use biotype and temp stable ID as display xref.
  my $dbname = $self->get_stable_id($eobj);

# temp stable ID is placed between pipe chars to later be replaced by the final ensembl stable ID.
  my $primary_id = $self->get_primary_id($eobj);
  my $display_id = $primary_id;
  my $display_xref;
  if ($dbname && $primary_id && $display_id) {
  $display_xref =
    Bio::EnsEMBL::DBEntry->new(-DBNAME     => $dbname,
                 -PRIMARY_ID => $primary_id,
                 -DISPLAY_ID => $display_id,
                 -ANALYSIS   => $self->{gl_name_xref});
  }
  return $display_xref;
}
my $stable_id_names = {
        NAMES()->{GENE}        => NAMES()->{GENE_STABLE_ID},
        NAMES()->{TRANSCRIPT}  => NAMES()->{TRANSCRIPT_STABLE_ID},
        NAMES()->{TRANSLATION} => NAMES()->{TRANSLATION_STABLE_ID}
};

sub get_stable_id {
  my ($self, $eobj) = @_;
  return $stable_id_names->{ref($eobj)};
}

sub get_primary_id {
  my ($self, $eobj) = @_;
  return $eobj->stable_id();
}

sub get_feature_display_xref_by_name {
  my ($self, $eobj, $dbname, $primary_id, $display_id) = @_;
  my $display_xref;
  if ($dbname && $primary_id && $display_id) {
  $display_xref =
    Bio::EnsEMBL::DBEntry->new(-DBNAME     => $dbname,
                 -PRIMARY_ID => $primary_id,
                 -DISPLAY_ID => $display_id,
                 -ANALYSIS   => $self->{gl_name_xref});
  $eobj->add_DBEntry($display_xref);
  }
  return $display_xref;
}

sub get_feature_display_xref {
  my ($self, $iobj, $eobj, $dbname, $iattr) = @_;
  my $display_id = $iobj->{$iattr};
  return
  $self->get_feature_display_xref_by_name($eobj, $dbname,
                      $display_id, $display_id);
}

sub get_gene_names_display_xref {
  my ($self, $iobj, $eobj, $dbname, $iattr) = @_;
  my $display_xref;
  if ($iobj->{names}->{$iattr}) {
  my @vals = @{$iobj->{names}->{$iattr}};
  if (@vals && $iobj->{names}) {
    my $val = shift @vals;
    if ($dbname && $val) {
    my $pid = $val;
    if ($self->{species_id}) {
      $pid .= ' ' . $self->{species_id};
    }
    $display_xref =
      Bio::EnsEMBL::DBEntry->new(-DBNAME     => $dbname,
                   -PRIMARY_ID => $pid,
                   -DISPLAY_ID => $val,
                   -ANALYSIS => $self->{gl_name_xref}
      );
    $eobj->add_DBEntry($display_xref);
    }
  }
  }
  if (defined $display_xref) {
  $self->add_synonyms($display_xref, $iobj);
  }
  return $display_xref;
} ## end sub get_gene_names_display_xref

sub add_synonyms {
  my ($self, $display_xref, $iobj) = @_;

  # add the rest of the names as synonyms
  for my $name_type (keys %{$iobj->{names}}) {
  my $names_list = $iobj->{names}->{$name_type};
  if (defined $names_list) {
    for my $name (@$names_list) {

    # if its not the actual name its a synonym
    if ($name ne $display_xref->display_id()) {
      $display_xref->add_synonym($name);
    }
    }
  }
  }
  return;
}

sub get_db_display_xref {
  my ($self, $iobj, $eobj, $display_xref_source) = @_;
  my @refs = grep { $_->dbname() eq $display_xref_source }
  @{$eobj->get_all_DBEntries()};
  return @refs ? $refs[0] : undef;
}

sub get_xref_type_for_gene {
  my ($self, $iobj, $eobj) = @_;
  my $type = $self->{gene_anal_2_xref}{$eobj->biotype()};
  if (!$type) {
  croak(
     "Cannot find xref type for gene of biotype " . $eobj->biotype());
  }
  if ($type && ref($type) eq 'HASH') {
  if (!$eobj->analysis()) {
    croak("Analysis type not set for gene of biotype " .
      $eobj->{biotype} . ":" . $eobj);
  }
  $type = $type->{$eobj->analysis()->{_logic_name}};
  }
  if (!$type) {
  croak("Could not find gene type for gene of biotype " .
      $eobj->biotype() .
      " with analysis type " . $eobj->analysis()->{_logic_name});
  }
  return $type;
}

sub get_xref_type_for_transcript {
  my ($self, $iobj, $eobj) = @_;
  my $type = $self->{transcript_anal_2_xref}{$eobj->biotype()};
  if (ref($type) eq 'HASH') {
  $type = $type->{$eobj->analysis()->{_logic_name}};
  }
  if (!$type) {
  croak("Could not find transcript type for transcript of biotype " .
      $eobj->biotype() .
      " for analysis type " . $eobj->analysis()->{_logic_name});
  }
  return $type;
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
  Returns  : new instance

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
  