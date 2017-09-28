
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
package Bio::EnsEMBL::GenomeLoader::GeneLoader;
use warnings;
use strict;
use Carp;
use Bio::EnsEMBL::DBEntry;
use Bio::EnsEMBL::OntologyXref;
use Bio::EnsEMBL::IdentityXref;
use Bio::EnsEMBL::GenomeLoader::Constants qw(XREFS NAMES);
use Bio::EnsEMBL::GenomeLoader::DisplayXrefFinder;
use Bio::EnsEMBL::GenomeLoader::StableIdFinder qw/get_stable_id/;
use Bio::EnsEMBL::GenomeLoader::AnalysisFinder qw/get_analysis_by_name/;

use Data::Dumper;
use Scalar::Util qw(looks_like_number);
use base qw(Bio::EnsEMBL::GenomeLoader::BaseLoader);

sub new {
  my $caller = shift;
  my $class  = ref($caller) || $caller;
  my $self   = $class->SUPER::new(@_);
  $self->{displayXrefFinder} =
    Bio::EnsEMBL::GenomeLoader::DisplayXrefFinder->new( -DBA => $self->dba() );

  $self->{gene_analysis} = get_analysis_by_name( 'protein_coding', 'gene' );
  $self->{xrefanalysis}  = get_analysis_by_name('gl_xref');
  $self->{gl_name_xref}  = get_analysis_by_name('gl_name_xref');
  $self->{source} = 'ena';

  return $self;
}

sub load_gene {
  my ( $self, $igenes, $slice ) = @_;
  croak("store_gene cannot be invoked on the base class");
}

sub get_alignment_analysis {
  my $self    = shift;
  my $aliname = 'eg_alignment';
  return get_analysis_by_name( $aliname, 'gene' );
}

sub get_go_xref {
  my ( $self, $xref ) = @_;
  my $ensembl_dbname = $xref->{databaseReferenceType};
  my $dbentry =
    Bio::EnsEMBL::OntologyXref->new( -DBNAME     => $ensembl_dbname,
                                     -PRIMARY_ID => $xref->{primaryIdentifier},
                                     -DISPLAY_ID => $xref->{primaryIdentifier},
                                     -ANALYSIS   => $self->{xrefanalysis} );
  my $linkage_type = $xref->{secondaryIdentifier};
  if ($linkage_type) {
    $dbentry->add_linkage_type( $linkage_type,
                                $self->get_src_dbentry( $xref->{source} ) );
  }

# TODO can also add source as well - this should end up in quaternaryIdentifier - however, need to modify go_xref table and OntologyXref object and also adaptor
  return $dbentry;
}

sub get_src_dbentry {
  my ( $self, $xref ) = @_;
  my $dbentry =
    Bio::EnsEMBL::DBEntry->new(
      -DBNAME     => $xref->{databaseReferenceType},
      -PRIMARY_ID => $xref->{primaryIdentifier},
      -DISPLAY_ID => $xref->{secondaryIdentifier} || $xref->{primaryIdentifier},
      -DESCRIPTION => $xref->{description},
      -ANALYSIS    => $self->{xrefanalysis},
      -VERSION     => $xref->{version} );
  return $dbentry;
}

sub get_generic_xref {
  my ( $self, $xref, $eobject ) = @_;
  my $dbentry;
  my $primary_id     = $xref->{primaryIdentifier};
  my $display_id     = $primary_id;
  my $ensembl_dbname = $xref->{databaseReferenceType};
  my $eobject_type   = ref($eobject);
  my $version        = $xref->{version};
  my $description    = $xref->{description};

  # TODO make this much neater, maybe with a dispatch map
  croak(
      "Ensembl dbname not set for " . Dumper( $xref->{databaseReferenceType} ) )
    if ( !$ensembl_dbname );

  if ( $ensembl_dbname eq XREFS()->{PROTEIN_ID} &&
       $xref->{quarternaryIdentifier} eq XREFS()->{GENOMIC_DNA} )
  {

    # PROTEIN_ID && GENOMIC_DNA.
    $primary_id = $xref->{secondaryIdentifier};
    $display_id = $xref->{secondaryIdentifier} || $xref->{primaryIdentifier};
  }
  elsif ( $ensembl_dbname eq XREFS()->{PROTEIN_ID} ) {

    # PROTEIN_ID && NOT GENOMIC_DNA
    $display_id = $xref->{secondaryIdentifier} || $xref->{primaryIdentifier};

  }
  elsif ( $ensembl_dbname eq XREFS()->{RFAM} ) {
    $display_id = $xref->{secondaryIdentifier};
  }
  elsif ( $ensembl_dbname eq XREFS()->{INTACT} && defined $xref->{source} ) {
    $primary_id = $xref->{source}{primaryIdentifier};
  }
  elsif ( uc $ensembl_dbname eq uc XREFS()->{INTERPRO} ) {

    # INTERPRO
    if ( $xref->{description} ) {

      # DESCRIPTION PRESENT.
      my @description = split( '\|\|', $xref->{description} );
      if ( @description == 2 ) {
        my ( $short, $long ) = @description;
        $display_id = $short;
        $xref->{description} = $long;
      }
      else {

        # DESCRIPTION DOES NOT contain short and long versions.
        $display_id  = $xref->{description};
        $description = undef;
      }
    }
  }

  if ( !defined $display_id || $display_id eq '' ) {
    $display_id = $primary_id;
  }

  if ( defined $xref->{identityXref} &&
       $eobject_type eq NAMES()->{TRANSLATION} &&
       ( $ensembl_dbname eq XREFS()->{UNIPROT_SWISSPROT} ||
         $ensembl_dbname eq XREFS()->{UNIPROT_TREMBL} ) )
  {

# For UniProt/Swiss-Prot and UniProt/TrEMBL, where $xref->{identityXref} is true:
# Create an identity xref so that DAS finds a coord mapper and positional features are shown.
# When the Integr8 data is fixed it will only be necessary to check $xref->{identityXref}.
    $dbentry = Bio::EnsEMBL::IdentityXref->new(
      -DBNAME           => $ensembl_dbname,
      -PRIMARY_ID       => $primary_id,
      -DISPLAY_ID       => $display_id,
      -XREF_IDENTITY    => 100,
      -ENSEMBL_IDENTITY => 100,
      #-EVALUE          => 0,
      -SCORE => 100,

# Note: using $eobject->length will fail as the Translation hasn't been stored yet.
      -CIGAR_LINE    => ( $eobject->end - $eobject->start + 1 ) . 'M',
      -XREF_START    => $eobject->start,
      -XREF_END      => $eobject->end,
      -ENSEMBL_START => $eobject->start,
      -ENSEMBL_END   => $eobject->end,
      -DESCRIPTION   => $description,
      -ANALYSIS      => $self->{xrefanalysis},
      -VERSION       => $version );
  } ## end if ( defined $xref->{identityXref...})
  else {
    if ( !defined $primary_id ) {
      die Dumper($xref);
    }
    $dbentry =
      Bio::EnsEMBL::DBEntry->new( -DBNAME      => $ensembl_dbname,
                                  -PRIMARY_ID  => $primary_id,
                                  -DISPLAY_ID  => $display_id,
                                  -DESCRIPTION => $description,
                                  -ANALYSIS    => $self->{xrefanalysis},
                                  -VERSION     => $version );
  }

  if ( defined $dbentry->{version} ) {
    if ( looks_like_number( $dbentry->{version} ) && $dbentry->{version} == 0 )
    {
      $dbentry->{version} = undef;
    }
    elsif ( $dbentry->{version} eq '' ) {
      $dbentry->{version} = undef;
    }
  }

  return $dbentry;
} ## end sub get_generic_xref

sub add_xref {
  my ( $self, $xref, $eobject ) = @_;
  # Get ensembl dbname.
  my $ensembl_dbname = $xref->{databaseReferenceType};
  my $dbentry;
  if ( $ensembl_dbname eq XREFS()->{GO} ) {
    $dbentry = $self->get_go_xref($xref);
  }
  else {
    $dbentry = $self->get_generic_xref( $xref, $eobject );
  }
  if ( $xref->{description} ) {
    $dbentry->description( $xref->{description} );
  }
  if ( defined $dbentry->primary_id() ) {
    $eobject->add_DBEntry($dbentry);
  }
  else {
    carp( "primary_id not set for " . Dumper($dbentry) )
      if ( !$dbentry->primary_id() || $dbentry->primary_id() eq '' );
  }
  croak(
      "Ensembl dbname not set for " . $xref->{databaseReferenceType} )
    if ( !$ensembl_dbname || $ensembl_dbname eq '' );
  croak( "dbname not set for " . Dumper($dbentry) )
    if ( !$dbentry->dbname() || $dbentry->dbname() eq '' );
} ## end sub add_xref

sub set_xrefs_from_list {
  my ( $self, $irefs, $eobject ) = @_;
  # non-GO first to allow sources to be added
  foreach my $xref (
    grep {
      $_->{databaseReferenceType} ne XREFS()->{GO}
    } @$irefs )
  {
    $self->add_xref( $xref, $eobject );

  }

  # then GO
  foreach my $xref (
    grep {
      ( $_->{databaseReferenceType} eq XREFS()->{GO} )
    } @$irefs )
  {
    $self->add_xref( $xref, $eobject );
  }
  return;
}

sub set_xrefs {
  my ( $self, $iobject, $eobject ) = @_;

  # remove suppressed refs
  my @irefs = grep { !$_->{suppress} } @{ $iobject->{databaseReferences} };
  $self->set_xrefs_from_list( \@irefs, $eobject );

  return;
}

sub set_stable_id {
  my ( $self, $iobj, $eobj, $parent, $index ) = @_;
  my $stable_id =
    get_stable_id( $iobj, $eobj, $parent, $index );
  if ($stable_id) {
    $eobj->stable_id($stable_id);
  }
  else {
    $self->log()->warn("Could not get stable_id");
  }
  return;
}

sub set_display_xref {
  my ( $self, $iobj, $eobj, $parent, $index ) = @_;
  my $display_xref =
    $self->{displayXrefFinder}
    ->get_display_xref( $iobj, $eobj, $parent, $index );
  if ( defined $display_xref  && ( $display_xref->dbname() &&
          $display_xref->primary_id() &&
          $display_xref->display_id() ))
  {

    # Set the display xref for gene/transcript.
    $eobj->display_xref($display_xref);
  }
  else {
    $self->log()
      ->debug(
           "Could not store display_xref as primary ID or display ID missing" );
  }
  return;
}

sub store_gene_error_handler {
  my ( $self, $igene, $egene ) = @_;
  my $err = sprintf( "Failed storing %s %s in sequence region name %s\n%s\n",
                     $igene->{biotype}, $egene->stable_id,
                     $egene->slice()->seq_region_name, $@ );
  croak($err);
  exit;
}
1;
__END__

=head1 NAME

GenomeLoader::GeneLoader

=head1 SYNOPSIS

Base module to load a specified gene hash into an ensembl database. Specific implementations needed for specific gene types e.g. protein_coding etc,.

=head1 AUTHORS

Dan Staines <dstaines@ebi.ac.uk>

=head1 METHODS

=head2 new
  Title      : new
  Description: Constructor. Invokes BaseLoader->new as well
  Args       : Hash of arguments
  Returns    : new instance

=head2 load_gene
  Title      : load_geme
  Description: empty stub for loading gene. Should be overridden in child modules.
  Args       : gene hash
  Returns	 : Bio::Ensembl::Gene

=head2 get_alignment_analysis
  Title      : get_alignment_analysis
  Description: retrieve the correct analysis object for the current data division
  Returns 	 : Bio::Ensembl::Analysis

=head2 get_alignment_location
  Title      : get_alignment_location
  Description: retrieve the correct analysis object for the supplied location e.g. EMBLBANK, EBACTERIA etc.
  Args		 : location hash
  Returns 	 : Bio::Ensembl::Analysis

=head2 get_generic_xref
  Title      : get_generic_xref
  Description: turn the supplied xref into an ensembl object
  Args		 : xref hash
  Returns 	 : Bio::Ensembl::DBEntry

=head2 get_go_xref
  Title      : get_go_xref
  Description: turn the supplied xref into an ensembl GO Xref
  Args		 : xref hash
  Returns 	 : Bio::Ensembl::OntologyXref

=head2 set_display_xref
  Title      : set_display_xref
  Description: use the appropriate displayXrefFinder to find the best display xref for the current object
  Args		 : Bio::Ensembl::Gene, gene hash, parent hash, child index
  Returns 	 : Bio::Ensembl::DBEntry

=head2 set_xrefs
  Title      : set_xrefs
  Description: turn the xrefs from the gene hash into ensembl xrefs and ttach to the current gene
  Args		 : gene hash, Bio::Ensembl::Gene
