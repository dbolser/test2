
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
package Bio::EnsEMBL::GenomeLoader::GeneLoader::PseudoGeneLoader;
use warnings;
use strict;
use Bio::EnsEMBL::Gene;
use Bio::EnsEMBL::Exon;
use Bio::EnsEMBL::Transcript;
use Carp;
use Bio::EnsEMBL::GenomeLoader::Constants qw(XREFS NAMES);
use base qw(GenomeLoader::GeneLoader);

sub new {
  my $caller = shift;
  my $class  = ref($caller) || $caller;
  my $self   = $class->SUPER::new(@_);
  return $self;
}

sub load_gene {

  my ( $self, $igene, $slice ) = @_;

  # Stable ID suffix index.
  my $feature_index = 1;

# Remove swissprot and trembl xrefs from the gene. (Move EMBL-CDS xrefs from the gene and add them to the transcript later.)
  my @igene_xrefs;
  my @itranscript_xrefs;
  foreach my $xref ( @{ $igene->{xrefs} } ) {
    my $ensembl_dbname = $xref->{databaseReferenceType}{ensemblName};
    if ( $ensembl_dbname ne XREFS()->{UNIPROT_SWISSPROT} &&
         $ensembl_dbname ne XREFS()->{UNIPROT_TREMBL} )
    {

      # Keep all other gene xrefs.
      push( @igene_xrefs, $xref );
    }
  }
  $igene->{xrefs} = \@igene_xrefs;

  # Choose analysis track based upon the gene location mapping type.
  my $gene_analysis =
    $self->get_analysis_for_location(
                                    $igene->{locations}->[0]->{state} );
  my $time = time();
  my $egene =
    Bio::EnsEMBL::Gene->new( -SLICE        => $slice,
                             -BIOTYPE      => $igene->{biotype},
                             -SOURCE       => $self->config()->{source},
                             -ANALYSIS     => $gene_analysis,
                             -CREATED_DATE => $time,
                             -MODIFIED_DATE => $time );

  # Gene description.
  $egene->description( $igene->{description} )
    if ( $igene->{description} );
  $self->set_stable_id( $igene, $egene );

  # Set gene xrefs (including GO xrefs).
  $self->set_xrefs( $igene, $egene );

  # Set gene display xref.
  $self->set_display_xref( $igene, $egene );

  # Create exons.  There is only one location for pseudogenes.
  my @eexons;
  my $exonN = 0;
  foreach my $igene_location ( @{ $igene->{locations} } ) {
    $time = time();
    my $eexon = Bio::EnsEMBL::Exon->new(
      -SLICE  => $slice,
      -START  => $igene_location->{min},
      -END    => $igene_location->{max},
      -STRAND => $igene_location->{strand},

      # Forced phase to 0
      -PHASE         => 0,
      -END_PHASE     => 0,
      -CREATED_DATE  => $time,
      -MODIFIED_DATE => $time );
    $self->set_stable_id( {}, $eexon, $egene, ++$exonN );
    push( @eexons, $eexon );
    $self->set_stable_id( $igene, $egene );
  }

  my $transcriptN = 0;
  # Create transcripts.
  foreach my $igene_location ( @{ $igene->{locations} } ) {

    # Create a dummy Integr8 transcript with EMBL-CDS xrefs.
    my $itranscript;
    $itranscript->{xrefs} = \@itranscript_xrefs;
    my $time = time();
    my $etranscript =
      Bio::EnsEMBL::Transcript->new( -BIOTYPE  => $egene->biotype(),
                                     -SLICE    => $slice,
                                     -ANALYSIS => $egene->analysis(),
                                     -CREATED_DATE  => $time,
                                     -MODIFIED_DATE => $time );

    # Add exons to transcripts.
    foreach my $eexon (@eexons) {
      $etranscript->add_Exon($eexon);
    }

    # Set transcript xrefs (including GO xrefs).
    $self->set_xrefs( $itranscript, $etranscript );

    # Set transcript display xref.
    $self->set_display_xref( $itranscript, $etranscript,
                             $egene,       ++$transcriptN );
    $self->set_stable_id( $itranscript, $etranscript,
                          $egene,       $transcriptN );
    # Add transcript to gene.
    $egene->add_Transcript($etranscript);
  } ## end foreach my $igene_location ...

  # Store the gene, but only if it has at least one transcript.
  if ( @{ $egene->get_all_Transcripts() } > 0 ) {
    eval { $self->dba()->get_GeneAdaptor->store($egene); };
    if ($@) {
      croak( $self->store_gene_error_handler( $igene, $egene ) );
    }
  }
  return;
} ## end sub load_gene

1;
__END__

=head1 NAME

GenomeLoader::FeatureLoader::PseudoGeneLoader

=head1 SYNOPSIS

Create a new gene for a pseudogene

=head1 AUTHORS

Dan Staines <dstaines@ebi.ac.uk>

=head1 METHODS

=head2 new
  Title      : new
  Description: Constructor. Invokes BaseLoader->new as well
  Args       : Hash of arguments
  Returns    : new instance

=head2 load_gene
  Title      : load_gene
  Description: Create new Bio::EnsEMBL::Gene object based on supplied hash. Also creates a transcript but no exons or transaltion
  Args       : gene hash, slice
  Returns    : Bio::EnsEMBL::Gene
