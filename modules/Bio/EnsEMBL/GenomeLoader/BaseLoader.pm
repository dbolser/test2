
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
# Base object for all classes responsible for loading data into an ensembl database
#
package Bio::EnsEMBL::GenomeLoader::BaseLoader;
use warnings;
use strict;
use Carp;
use Bio::EnsEMBL::GenomeLoader::Constants qw(CS BIOTYPES);
use Data::Dumper;
use Log::Log4perl qw(get_logger);
use Bio::EnsEMBL::Utils::Argument qw( rearrange );
use Bio::EnsEMBL::Utils::ScriptUtils qw(inject);

my $loader_names = {
            sequence => "Bio::EnsEMBL::GenomeLoader::SequenceLoader",
            simplefeature =>
              "Bio::EnsEMBL::GenomeLoader::FeatureLoader::SimpleFeatureLoader",
            repeatfeature =>
              "Bio::EnsEMBL::GenomeLoader::FeatureLoader::RepeatFeatureLoader",
            BIOTYPES()->{PROTEIN_CODING_GENE_TYPE} =>
              "Bio::EnsEMBL::GenomeLoader::GeneLoader::ProteinCodingGeneLoader",
            BIOTYPES()->{PSEUDOGENE_TYPE} =>
              "Bio::EnsEMBL::GenomeLoader::GeneLoader::ProteinCodingGeneLoader",
            BIOTYPES()->{NON_TRANSLATING_TYPE} =>
              "Bio::EnsEMBL::GenomeLoader::GeneLoader::ProteinCodingGeneLoader",
            BIOTYPES()->{NCRNA_TYPE} =>
              "Bio::EnsEMBL::GenomeLoader::GeneLoader::RnaGeneLoader",
            BIOTYPES()->{MISC_RNA_TYPE} =>
              "Bio::EnsEMBL::GenomeLoader::GeneLoader::RnaGeneLoader",
            BIOTYPES()->{ANTISENSE_TYPE} =>
              "Bio::EnsEMBL::GenomeLoader::GeneLoader::RnaGeneLoader",
            BIOTYPES()->{RNASEP_TYPE} =>
              "Bio::EnsEMBL::GenomeLoader::GeneLoader::RnaGeneLoader",
            BIOTYPES()->{RNASEMRP_TYPE} =>
              "Bio::EnsEMBL::GenomeLoader::GeneLoader::RnaGeneLoader",
            BIOTYPES()->{TMRNA_TYPE} =>
              "Bio::EnsEMBL::GenomeLoader::GeneLoader::RnaGeneLoader",
            BIOTYPES()->{SRP_RNA_TYPE} =>
              "Bio::EnsEMBL::GenomeLoader::GeneLoader::RnaGeneLoader",
            BIOTYPES()->{SN_RNA_TYPE} =>
              "Bio::EnsEMBL::GenomeLoader::GeneLoader::RnaGeneLoader",
            BIOTYPES()->{S_RNA_TYPE} =>
              "Bio::EnsEMBL::GenomeLoader::GeneLoader::RnaGeneLoader",
            BIOTYPES()->{SNO_RNA_TYPE} =>
              "Bio::EnsEMBL::GenomeLoader::GeneLoader::RnaGeneLoader",
            BIOTYPES()->{RRNA_TYPE} =>
              "Bio::EnsEMBL::GenomeLoader::GeneLoader::RRNAGeneLoader",
            BIOTYPES()->{MIRNA_TYPE} =>
              "Bio::EnsEMBL::GenomeLoader::GeneLoader::RnaGeneLoader",
            BIOTYPES()->{ANTITOXIN_TYPE} =>
              "Bio::EnsEMBL::GenomeLoader::GeneLoader::RnaGeneLoader",
            BIOTYPES()->{CRISPR_TYPE} =>
              "Bio::EnsEMBL::GenomeLoader::GeneLoader::RnaGeneLoader",
            BIOTYPES()->{TRNA_TYPE} =>
              "Bio::EnsEMBL::GenomeLoader::GeneLoader::TRNAGeneLoader",
            BIOTYPES()->{RIBOZYME_TYPE} =>
              "Bio::EnsEMBL::GenomeLoader::GeneLoader::TRNAGeneLoader",
            BIOTYPES()->{TRNA_PSEUDO_TYPE} =>
              "Bio::EnsEMBL::GenomeLoader::GeneLoader::TRNAGeneLoader" };

sub new {
  my ( $caller, @args ) = @_;
  my $class = ref($caller) || $caller;

  my $self = bless( {}, $class );

  ( $self->{dba}, $self->{taxonomy_dba}, $self->{production_dba} ) = rearrange( ['DBA', 'TAXONOMY_DBA', 'PRODUCTION_DBA'], @args );

  $self->{engine} = 'InnoDB';

  if ( !$self->log() ) {
    $self->log( get_logger() );
  }
  return $self;
}

sub use_transactions {
  my $self = shift;
  return $self->{engine} eq 'InnoDB';
}

sub dba {
  my $self = shift;
  $self->{dba} = shift if @_;
  return $self->{dba};
}

sub taxonomy_dba {
  my $self = shift;
  $self->{taxonomy_dba} = shift if @_;
  return $self->{taxonomy_dba};
}
sub production_dba {
  my $self = shift;
  $self->{production_dba} = shift if @_;
  return $self->{production_dba};
}

sub log {
  my $self = shift;
  $self->{log} = shift if @_;
  return $self->{log};
}

sub get_loader {
  my ( $self, $name ) = @_;
  my $loader = $self->{loaders}->{$name};
  if ( !defined $loader ) {
    my $loader_name = $loader_names->{$name};
    croak "Could not find loader for $name" unless defined $loader_name;
    inject($loader_name);
    $loader = $loader_name->new( -DBA => $self->dba() );
    $self->{loaders}->{$name} = $loader;
  }
  return $loader;
}

sub convert_dna_2_peptide_coords {
  my ( $self, $iobject_location, $seq_start, $seq_end, $offset ) = @_;

  if ( !defined $offset ) {
    $offset = 1;
  }
  $offset -= 1;

# Cannot calculate peptide coords if location is not exact.
# Get locations and sub-locations
# The one and only parent-level location of the iobject.
# Reference to an array of the one and only parent-level location of the iobject or its sub-locations (if there are any).
  my $ilocations;
  if ( $iobject_location->{sublocations} &&
       @{ $iobject_location->{sublocations} } )
  {

    # There are sub-locations, use them.
    $ilocations = $iobject_location->{sublocations};
  }
  else {

    # There are no sub-locations, use the parent location.
    $ilocations = [$iobject_location];
  }

  my $pep_start;
  my $pep_end;
  if ( $iobject_location->{strand} == 1 ) {

    # forward strand.
    my $seq_translated = 0;
    foreach my $ilocation ( sort { $a->{min} <=> $b->{min} } @{$ilocations} ) {
      if ( !$pep_start &&
           $seq_start >= $ilocation->{min} &&
           $seq_start <= $ilocation->{max} )
      {
        $pep_start = int(
            ( $seq_start - $ilocation->{min} + $seq_translated + $offset )/3 ) +
          1;
      }
      if ( !$pep_end &&
           $seq_end >= $ilocation->{min} &&
           $seq_end <= $ilocation->{max} )
      {
        $pep_end =
          int( ( $seq_end - $ilocation->{min} + $seq_translated + $offset )/3 )
          + 1;
      }
      $seq_translated += location_length($ilocation);
      last if ( $pep_start && $pep_end );
    }

# Peptide coord is outside coding region # But not in a gap between coding regions.
    if ( !$pep_start &&
         ( $seq_start < $iobject_location->{min} ||
           $seq_start > $iobject_location->{max} ) )
    {

      # Adjustment which only works if $seq_start < $f->location->start.
      $pep_start =
        int( ( $seq_start - $iobject_location->{min} + 1 + $offset )/3 );
    }

# Peptide coord is outside coding region  But not in a gap between coding regions
    if ( !$pep_end &&
         ( $seq_end < $iobject_location->{min} ||
           $seq_end > $iobject_location->{max} ) )
    {
      $pep_end = int(
         ( $seq_end - $iobject_location->{min} + $seq_translated + $offset )/3 )
        + 1;
    }
  } ## end if ( $iobject_location...)
  else {

    # reverse strand.
    my $seq_translated = 0;
    foreach
      my $ilocation ( reverse sort { $a->{min} <=> $b->{min} } @{$ilocations} )
    {
      if ( !$pep_start &&
           $seq_end >= $ilocation->{min} &&
           $seq_end <= $ilocation->{max} )
      {
        $pep_start =
          int( ( $ilocation->{max} - $seq_end + $seq_translated + $offset )/3 )
          + 1;
      }
      if ( !$pep_end &&
           $seq_start >= $ilocation->{min} &&
           $seq_start <= $ilocation->{max} )
      {
        $pep_end = int(
            ( $ilocation->{max} - $seq_start + $seq_translated + $offset )/3 ) +
          1;
      }
      $seq_translated += location_length($ilocation);
      last if ( $pep_start && $pep_end );
    }

# Peptide coord is outside coding region.     # But not in a gap between coding regions.
    if ( !$pep_start &&
         ( $seq_end < $iobject_location->{min} ||
           $seq_end > $iobject_location->{max} ) )
    {

      # Adjustment which only works if $seq_end > $f->location->end.
      $pep_start =
        int( ( $iobject_location->{max} - $seq_end + 1 + $offset )/3 );
    }

    # Peptide coord is outside coding region.
    if ( !$pep_end &&
         ( $seq_start < $iobject_location->{min} ||
           $seq_start > $iobject_location->{max} ) )
    {
      $pep_end = int(
          ( $iobject_location->{max} - $seq_start + $seq_translated + $offset )/
            3 ) + 1;
    }
  } ## end else [ if ( $iobject_location...)]
  return ( $pep_start, $pep_end );
} ## end sub convert_dna_2_peptide_coords

sub location_length {
  return abs( $_[0]->{min} - $_[0]->{max} ) + 1;
}

sub store_mapping_path {
  my $self     = shift;
  my @csystems = @_;
  my $csa      = $self->dba()->get_CoordSystemAdaptor();

  # Validate and sort the args
  my %seen_ranks;
  @csystems >= 2 or croak('Need two or more CoordSystems');
  $self->log()->debug("Storing mapping paths @csystems");
  my $validate = sub {
    ( ref( $_[0] ) && $_[0]->isa('Bio::EnsEMBL::CoordSystem') ) ||
      croak( 'CoordSystem argument expected: got ' . ref( $_[0] ) );
    my $rank = $_[0]->rank ||
      croak( 'CoordSystem has no rank: ' . $_[0]->name );
    $seen_ranks{$rank} &&
      croak( 'CoordSystem ' . $_[0]->name . " shares rank $rank with " .
             $seen_ranks{$rank}->name );
    $seen_ranks{$rank} = $_[0];
  };
  @csystems = sort { $a->rank <=> $b->rank } map { &{$validate}($_) } @csystems;
  my $meta = $self->dba()->get_MetaContainer();
  my @retlist;

  # For each pair in the sorted list, store in the DB
  for ( my $i = 1; $i < @csystems; $i++ ) {
    for ( my $j = 0; $j < ( @csystems - $i ); $j++ ) {

      my @sub_systems = @csystems[ $j .. $j + $i ];

      my $mapping;
      my $mapping_key;

      for my $cs (@sub_systems) {
        if ( defined $mapping ) {
          # add an element with an appropriate joiner
          my $joiner = ( $cs->name() eq CS()->{CONTIG} ) ? '#' : '|';
          $mapping     .= $joiner;
          $mapping_key .= $joiner;
        }
        $mapping     .= join( ':', $cs->name, ( $cs->version || () ) );
        $mapping_key .= join( ':', $cs->name, ( $cs->version || '' ) );
      }

      # Skip existing
      next if $csa->{'_mapping_paths'}->{$mapping_key};

      # Update the database
      $meta->store_key_value( 'assembly.mapping', $mapping );
      push @retlist, $mapping;
    } ## end for ( my $j = 0; $j < (...))
  } ## end for ( my $i = 1; $i < @csystems...)
  if (@retlist) {
    $csa->_cache_mapping_paths();
  }
  return [@retlist];
} ## end sub store_mapping_path

sub is_prokaryote {
  my ($self) = @_;
  if ( !defined $self->{is_prokaryote} ) {
    $self->{is_prokaryote} =
      $self->dba()->dbc()->sql_helper()
      ->execute_single_result(
      -SQL =>
"select count(*) from meta where species_id=? and meta_key='species.division' and meta_value='EnsemblBacteria'",
      -PARAMS => [ $self->dba()->species_id() ] );
  }
  return $self->{is_prokaryote};
}

1;
__END__

=head1 NAME

GenomeLoader::BaseLoader

=head1 SYNOPSIS

A module from which all GenomeLoader loader modules extend. Contains base object plus helper methods

=head1 AUTHORS

Dan Staines <dstaines@ebi.ac.uk>

=head1 METHODS

=head2 new
  Title      : new
  Description: Constructor for all implementing classes
  Args       : Hash of arguments
  Returns    : new instance

=head2 dba
  Title      : dba
  Description: get/set ensembl database adaptor.
  Args       : database adaptor (optional)
  Returns    : database adaptor

=head2 log
  Title      : log
  Description: get/set logger
  Args       : Log::Log4perl logger (optional)
  Returns    : Log::Log4perl logger

=head2 log
  Title      : store_mapping_path
  Description: write mapping path using the DBA. Uses EG-specific logic missing from the adaptor.
  Args       : array of coordinate systems
  Returns    : array of mappings

=head2 convert_dna_2_peptide_coords
  Title      : convert_dna_2_peptide_coords
  Description: helper method for transforming from DNA to peptide coords
  Args       : location hash, start, end
  Returns    : start, end

=head2 location_length
  Title      : location_length
  Description: helper method for getting the length of a location
  Args       : location hash
  Returns    : length
