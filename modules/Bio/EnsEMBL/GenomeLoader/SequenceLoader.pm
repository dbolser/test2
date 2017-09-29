
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

# $Source$# $Revision$
# $Date$
# $Author$
#
# Object for loading data into an ensembl database
#

package Bio::EnsEMBL::GenomeLoader::SequenceLoader;
use warnings;
use strict;
use Carp;
use Data::Dumper;
use Bio::EnsEMBL::Slice;
use Bio::EnsEMBL::Attribute;
use Bio::EnsEMBL::GenomeLoader::Constants qw(CS);
use base qw(Bio::EnsEMBL::GenomeLoader::BaseLoader);

sub new {
  my $caller = shift;
  my $class  = ref($caller) || $caller;
  my $self   = $class->SUPER::new(@_);
  return $self;
}

sub get_coord_systems {
  my ( $self, $assembly ) = @_;
  if ( !defined $self->{coord_systems} ) {
    # Create plasmid coord system.
    my $contig_cs = $self->get_coord_system( CS()->{CONTIG}, 1, 4, 1 );
    my $supercontig_cs =
      $self->get_coord_system( CS()->{SUPERCONTIG}, 0, 3, 1, $assembly );
    my $chr_cs =
      $self->get_coord_system( CS()->{CHROMOSOME}, 0, 1, 1, $assembly );
    my $plasmid_cs =
      $self->get_coord_system( CS()->{PLASMID}, 0, 2, 1, $assembly );

    $self->{coord_systems} = { CS()->{CHROMOSOME}    => $chr_cs,
                               CS()->{MITOCHONDRION} => $chr_cs,
                               CS()->{CHLOROPLAST}   => $chr_cs,
                               CS()->{PLASMID}       => $plasmid_cs,
                               CS()->{CONTIG}        => $contig_cs,
                               CS()->{SUPERCONTIG}   => $supercontig_cs,
                               CS()->{SCAFFOLD}      => $supercontig_cs };
  }
  return $self->{coord_systems};
}

sub get_coord_system {
  my ( $self, $name, $seq_level, $rank, $default, $version ) = @_;
  my $csa = $self->dba()->get_CoordSystemAdaptor();
  my $cs  = $csa->fetch_by_name($name);
  if ( !defined $cs ) {
    $cs = Bio::EnsEMBL::CoordSystem->new( -NAME           => $name,
                                          -SEQUENCE_LEVEL => $seq_level,
                                          -RANK           => $rank,
                                          -DEFAULT        => $default,
                                          -VERSION        => $version );
  }
  return $cs;
}

sub load_sequence {
  my ( $self, $icomponent, $assembly ) = @_;

  my $slice_adaptor = $self->dba()->get_SliceAdaptor;
  my $aa            = $self->dba()->get_AttributeAdaptor;
  # Store coord system.
  my $csa = $self->dba()->get_CoordSystemAdaptor;

  my $coord_systems = $self->get_coord_systems($assembly);

  my ( $component_name_prefix, $component_name ) =
    $self->parse_description($icomponent);
  $self->log()->info("Handling $component_name_prefix, $component_name");
  my $slice_coord_system = $coord_systems->{$component_name_prefix};
  $self->log()->debug( "Using coord system " . $slice_coord_system->name() );

  # reformat troublesome characterspp
  $component_name =~ s/\+/plus/g;
  $component_name =~ s/[^A-z0-9:.-]+/_/g;
  $component_name =~ s/_+/_/g;

  # Component slice.
  my $slice = Bio::EnsEMBL::Slice->new(
                                     -SEQ_REGION_NAME => $component_name,
                                     -COORD_SYSTEM    => $slice_coord_system,
                                     -START           => 1,
                                     -END             => $icomponent->{length},
                                     -SEQ_REGION_LENGTH => $icomponent->{length}
  );

  # Has the coord system been stored yet ? possible race condition
  if ( !defined $slice->coord_system()->dbID() ||
       $slice->coord_system()->dbID() == 0 )
  {
    $self->log()
      ->info( "Storing coord system " . $slice->coord_system()->name() );

    # Store coord system.
    $csa->store( $slice->coord_system );

  }

  if ( defined $icomponent->{sequence} ) {
    my $seq = $icomponent->{sequence}{sequence};
    $slice_adaptor->store( $slice, \$seq );
  }
  else {
    $slice_adaptor->store($slice);
  }

  my @attributes = ();
  if ( $icomponent->{topLevel} ) {
    push @attributes,
      Bio::EnsEMBL::Attribute->new(
                             -CODE        => 'toplevel',
                             -NAME        => 'Top Level',
                             -DESCRIPTION => 'Top Level Non-Redundant Sequence',
                             -VALUE       => '1' );

    my $long_name = $self->get_longname($icomponent);
    if ( defined $long_name && $long_name ne 'unknown' ) {
      push @attributes,
        Bio::EnsEMBL::Attribute->new( -CODE        => 'name',
                                      -NAME        => 'Name',
                                      -DESCRIPTION => 'User-friendly name',
                                      -VALUE       => $long_name );
    }
    if ( $icomponent->{metaData}{geneticCode} ne '1' ) {
      push @attributes,
        Bio::EnsEMBL::Attribute->new( -CODE        => 'codon_table',
                                      -NAME        => 'Codon table ID',
                                      -DESCRIPTION => 'Codon table ID',
                                      -VALUE       => $icomponent->{metaData}{geneticCode}
        );
    }
  } ## end if ( $icomponent->{topLevel...})
  
  if(defined $icomponent->{metaData}{description}) {
     push @attributes,
        Bio::EnsEMBL::Attribute->new( -CODE        => 'description',
                                      -VALUE       => $icomponent->{metaData}{description}
        );
  }

  if ( $slice->coord_system_name() eq CS()->{CHROMOSOME} ||
       $slice->coord_system_name() eq CS()->{PLASMID} )
  {
    push(
      @attributes,
      Bio::EnsEMBL::Attribute->new(
        -CODE => 'karyotype_rank',
        -NAME => 'Rank in the karyotype',
        -DESCRIPTION =>
'For a given seq_region, if it is part of the species karyotype, will indicate its rank',
        -VALUE => $icomponent->{metaData}{karyotypeRank} ) );
  }

  # Add circular sequence attribute to top-level seq_region.
  if ( $icomponent->{metaData}{circular} ) {
    push( @attributes,
          Bio::EnsEMBL::Attribute->new( -CODE        => 'circular_seq',
                                        -NAME        => 'Circular sequence',
                                        -DESCRIPTION => 'Circular sequence',
                                        -VALUE       => '1' ) );
  }

  # add any references
  my $xa = $self->dba()->get_DBEntryAdaptor();
  for my $xref ( @{ $icomponent->{xrefs} } ) {
    my $dbentry =
      Bio::EnsEMBL::DBEntry->new(
       -DBNAME     => $xref->{databaseReferenceType}{ensemblName},
       -PRIMARY_ID => $xref->{primaryIdentifier},
       -DISPLAY_ID => $xref->{secondaryIdentifier} || $xref->{primaryIdentifier}
      );
    my $dbX = $xa->store($dbentry);
    push( @attributes,
          Bio::EnsEMBL::Attribute->new(
                          -CODE        => 'xref_id',
                          -NAME        => 'Xref ID',
                          -DESCRIPTION => 'ID of associated database reference',
                          -VALUE       => $dbX ) );
  }

  if ( defined $icomponent->{creationDate} ) {
    push( @attributes,
          Bio::EnsEMBL::Attribute->new(
                                  -CODE        => 'creation_date',
                                  -NAME        => 'Creation date',
                                  -DESCRIPTION => 'Creation date of annotation',
                                  -VALUE => $icomponent->{creationDate}{date} )
    );
  }
  if ( defined $icomponent->{updateDate} ) {
    push( @attributes,
          Bio::EnsEMBL::Attribute->new(
                               -CODE        => 'update_date',
                               -NAME        => 'Update date',
                               -DESCRIPTION => 'Last update date of annotation',
                               -VALUE       => $icomponent->{updateDate}{date} )
    );
  }

  $self->log()->debug("Storing attributes on slice");

  # Store attributes on the top-level seq_region.
  $aa->store_on_Slice( $slice, \@attributes );

  my $syna = $slice_adaptor->db->get_SeqRegionSynonymAdaptor();
  my $vacc = $icomponent->{accession} . '.' . $icomponent->{metaData}{version};
  if ( $vacc ne $component_name ) {
    $syna->store( Bio::EnsEMBL::SeqRegionSynonym->new(
                                   -synonym        => $vacc,
                                   -external_db_id => 50710,
                                   -seq_region_id => $slice->get_seq_region_id()
                  ) );
  }
  else {
    $aa->store_on_Slice( $slice, [
                           Bio::EnsEMBL::Attribute->new(
                                            -CODE        => 'external_db',
                                            -NAME        => 'External database',
                                            -DESCRIPTION => 'External database',
                                            -VALUE       => 'ENA' ) ] );
  }

  return $slice;
} ## end sub load_sequence

sub parse_description {
  my ( $self, $icomponent ) = @_;
  my $component_name = $icomponent->{metaData}{name} ||
    $icomponent->{metaData}{accession};
  my $component_name_prefix = lc $icomponent->{metaData}{componentType};
  return ( $component_name_prefix, $component_name );
}

sub get_longname {
  my ( $self, $icomponent ) = @_;
  my $longname;
  if ( $icomponent->{genome}->{shortName} && $icomponent->{description} ) {
    $longname =
      $icomponent->{genome}->{shortName} . ' ' . $icomponent->{description};
  }
  else {
    $longname = 'unknown';
  }
  return $longname;
}

1;
__END__

=head1 NAME

GenomeLoader::SequenceLoader

=head1 SYNOPSIS

Module to load a specified component sequence into an ensembl database

=head1 AUTHORS

Dan Staines <dstaines@ebi.ac.uk>

=head1 METHODS

=head2 new
  Title      : new
  Description: Constructor. Invokes BaseLoader->new as well
  Args       : Hash of arguments
  Returns    : new instance

=head2 genome_metadata
  Title      : genome_metadata
  Description: get/set hash containing general metadata about a genome e.g. names, versions, files etc.
  Args       : metadata hash to set
  Returns    : metadata hash

=head2 load_sequence
  Title      : load_sequence
  Description: create coord system for the supplied sequence and store the sequence into the ensembl database.
  Args       : component hash

  =head2 parse_description
  Title      : parse_description
  Description: Parse the component description to get the coord_system type and name to use e.g Chromosome,1
  Args       : component hash
  Returns    : tuple of coord_system name and seq_region name 
  
  =head2 get_longname
  Title      : get_longname
  Description: Build a user-friendly name for the component
  Args       : name

