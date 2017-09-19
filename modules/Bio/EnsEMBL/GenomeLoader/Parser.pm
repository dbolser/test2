
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
# Module for parsing integr8 JSON dumps into a nested hash represneting a component
package Bio::EnsEMBL::GenomeLoader::Parser;
use warnings;
use strict;
use Carp;
use Bio::EnsEMBL::GenomeLoader::Utils qw(from_json_file_default);
use Bio::EnsEMBL::GenomeLoader::Constants qw(NAMES BIOTYPES XREFS);
use Exporter 'import';
use Data::Dumper;
use Scalar::Util qw(looks_like_number);
my $xref_details = {};

sub new {
  my ($class) = shift;
  $class = ref($class) || $class;
  my $self =
    ( @_ && defined $_[0] && ( ref( $_[0] ) eq 'HASH' ) ) ? $_[0] : {@_};
  bless( $self, $class );
  return $self;
}

sub log {
  my $self = shift;
  $self->{log} = shift if @_;
  return $self->{log};
}

sub config {
  my $self = shift;
  $self->{config} = shift if @_;
  return $self->{config};
}

sub parse {
  my ( $self, $data_dir ) = @_;
  $data_dir .= '/';
  $self->log()->info("Parsing genome from $data_dir");
  my $genome = from_json_file_default( $data_dir . 'genome' );
  $self->log()->info("Parsing components from $data_dir");
  my $componentsById =
    $self->parse_components( $data_dir, $genome->{persistableId} );
  $self->log()->info("Parsing genes from $data_dir");
  $self->add_genes( $data_dir, $componentsById );
  $self->log()->info("Parsing repeats and features from $data_dir");
  $self->add_repeats( $data_dir, $componentsById );
  $self->set_object_xrefs( $componentsById,
                           from_json_file_default( $data_dir . 'componentxref' )
  );

  for my $com ( values( %{$componentsById} ) ) {
    push @{ $genome->{components} }, $com;
  }
  $self->log()->info("FINISHED INTEGR8 GENOME from $data_dir");
  return $genome;
}

sub parse_components {
  my ( $self, $data_dir, $genomeId ) = @_;
  my $icomponents    = from_json_file_default( $data_dir . 'component' );
  my $componentsById = {};
  my $n              = 1;
  for my $icomponent ( @{ $icomponents->{$genomeId} } ) {
    $icomponent->{name} =~
      s/[^A-z0-9.-]+//g;    # replace characters that Ensembl cannot handle
    $icomponent->{rank} = $n++;
    $componentsById->{ $icomponent->{persistableId} } = $icomponent;
  }
  return $componentsById;
}

sub add_genes {
  my ( $self, $data_dir, $componentsById ) = @_;

  $self->add_protein_coding_genes( $data_dir, $componentsById );

  $self->add_pseudogenes( $data_dir, $componentsById );

  # rfamscan, RNAmmer and tRNAScan-SE genes
  $self->add_rnagenes( $data_dir . 'ncrnagene',
                       $data_dir . 'ncrnatranscript',
                       $componentsById );
  $self->add_rnagenes( $data_dir . 'rrnagene',
                       $data_dir . 'rrnatranscript',
                       $componentsById );
  $self->add_rnagenes( $data_dir . 'trnagene',
                       $data_dir . 'trnatranscript',
                       $componentsById );

  for my $component ( values %{$componentsById} ) {
    if ( !defined $component->{genes} ) {
      $component->{genes} = [];
    }
    $self->log()
      ->info( "Parsed " . $component->{accession} . " into " .
              scalar @{ $component->{genes} } . " genes" );
  }

  return;
} ## end sub add_genes

sub add_repeats {
  my ( $self, $data_dir, $componentsById ) = @_;

  for my $type (
     qw(dustrepeatregion trfrepeatregion repeatmaskerrepeatregion repeatregion))
  {
    $self->log()->info( "Parsing " . $type . " repeats" );
    $self->add_repeatfeatures( $componentsById, $data_dir . $type,
                               'repeatfeatures' );
  }
  for my $type (
    qw(alienhuntersimplefeature cpgsimplefeature eponinesimplefeature simplefeature)
    )
  {
    $self->log()->info( "Parsing " . $type . " features" );
    $self->add_repeatfeatures( $componentsById, $data_dir . $type,
                               'simplefeatures' );
  }

  return;
}

sub add_repeatfeatures {
  my ( $self, $componentsById, $filename, $feature_type ) = @_;
  my $features_by_id = from_json_file_default($filename);
  while ( my ( $component_id, $features ) = each(%$features_by_id) ) {
    my $component = $componentsById->{$component_id};
    my $i         = 0;
    foreach my $feature ( @{$features} ) {
      $self->log()
        ->info(
             "Adding repeat features " . $i++ . " to component $component_id" );
      $feature->{component_id} = $component_id;
      push @{ $component->{$feature_type} }, $feature;
    }
  }
  return;
}

sub add_protein_coding_genes {
  my ( $self, $data_dir, $componentsById ) = @_;

  # Protein coding genes.
  $self->log()->info("PROTEIN CODING GENES");

  # gene ID -> gene
  my $igeneid2gene =
    $self->add_genes_from_file( $data_dir . 'gene',
                                BIOTYPES()->{PROTEIN_CODING_GENE_TYPE},
                                $componentsById );
  $self->log()->info("LOAD INTEGR8 GENE NAMES");
  $self->add_gene_names( $data_dir . 'genename', $igeneid2gene );

  #	$self->log()->info("LOAD INTEGR8 GENE DESCRIPTIONS");
  #	add_gene_descriptions( $data_dir . 'genedescription', $igeneid2gene );

  # Set locations for each Integr8 gene object.
  $self->log()->info("LOAD INTEGR8 GENE LOCATIONS");
  $self->set_object_locations( $igeneid2gene,
                               from_json_file_default($data_dir . 'genelocation'
                               ),
                               {},
                               {} );

  # Set xrefs for each Integr8 gene object.
  $self->log()->info("LOADING INTEGR8 GENE XREFS");
  my $xrefs = from_json_file_default( $data_dir . 'genexref' );
  $self->log()->info("SETTING INTEGR8 GENE XREFS");
  $self->set_object_xrefs( $igeneid2gene, $xrefs );
  $self->add_proteins( $data_dir, $igeneid2gene );

  return;
} ## end sub add_protein_coding_genes

sub add_proteins {
  my ( $self, $data_dir, $igeneid2gene ) = @_;
  $self->log()->info("LOAD INTEGR8 PROTEINS");

  # protein ID -> protein
  my $iproteinid2protein =
    $self->add_proteins_from_file( $data_dir . 'protein', $igeneid2gene );

  # Set locations, sub-locations and location mods for each Integr8 protein
  $self->log()->info("SET INTEGR8 PROTEIN LOCATIONS");
  $self->set_object_locations( $iproteinid2protein,
                               from_json_file_default(
                                                   $data_dir . 'proteinlocation'
                               ),
                               from_json_file_default(
                                                $data_dir . 'proteinsublocation'
                               ),
                               from_json_file_default(
                                                $data_dir . 'proteinlocationmod'
                               ) );

  # Set xrefs for each Integr8 protein object.
  $self->log()->info("SET INTEGR8 PROTEIN XREFS");
  $self->set_object_xrefs( $iproteinid2protein,
                          from_json_file_default( $data_dir . 'proteinxref' ) );
  $self->log()->info("LOAD INTEGR8 PROTEIN FEATURES");

  # Must be called AFTER protein xrefs have been loaded and set
  # (as it depends on protein xrefs being assigned to protein).
  $self->add_protein_features( $data_dir . 'proteinfeature',
                               $iproteinid2protein );
  $self->add_transcripts( $data_dir, $iproteinid2protein );
  return;
} ## end sub add_proteins

sub add_proteins_from_file {
  my ( $self, $filename, $igeneid2gene ) = @_;
  my %iproteinid2protein;
  my $proteins = from_json_file_default($filename);
  while ( my ( $igene_id, $iproteins ) = each(%$proteins) ) {
    my $igene = $igeneid2gene->{$igene_id};
    foreach my $iprotein ( @{$iproteins} ) {
      $iprotein->{gene}         = $igene;
      $iprotein->{locations}    = [];
      $iprotein->{sublocations} = [];
      $iprotein->{transcripts}  = [];
      $iprotein->{features}     = [];
      $iprotein->{xrefs}        = [];
      push( @{ $igene->{proteins} }, $iprotein );
      $iproteinid2protein{ $iprotein->{persistableId} } = $iprotein;
    }
  }
  return \%iproteinid2protein;
}

sub add_protein_features {
  my ( $self, $filename, $iproteinid2protein ) = @_;
  my $feat = from_json_file_default($filename);
  while ( my ( $iprotein_id, $ifeatures ) = each( %{$feat} ) ) {
    my $iprotein = $iproteinid2protein->{$iprotein_id};
    $iprotein->{features} = $ifeatures;
    foreach my $ifeature ( @{$ifeatures} ) {
      if ( !defined $ifeature->{id} ) {
        $ifeature->{id} = $ifeature->{type};
      }
      $ifeature->{protein} = $iprotein;
    }
  }
  $self->add_interpro_features($iproteinid2protein);
  return;
}

sub add_interpro_features {
  my ( $self, $iproteinid2protein ) = @_;

  # Load Integr8 protein features from protein xrefs.
  foreach my $iprotein ( values %{$iproteinid2protein} ) {
    foreach my $xref ( @{ $iprotein->{xrefs} } ) {
      if ( uc $xref->{databaseReferenceType}->{ensemblName} eq
           uc XREFS()->{INTERPRO} )
      {
        my $iprotein_feature;
        # Type is the member DB type ID.
        $iprotein_feature->{name} = $xref->{secondaryIdentifier};

        # logic key is 4th ID
        $iprotein_feature->{type} = $xref->{quarternaryIdentifier};

        # Get start and end position from tertiaryIdentifier.
        my @location = split( '-', $xref->{tertiaryIdentifier} );
        if ( @location == 2 ) {
          $iprotein_feature->{start} = $location[0];
          $iprotein_feature->{end}   = $location[1];
        }
        if ( $iprotein_feature->{start} && $iprotein_feature->{end} ) {
          if ( $iprotein_feature->{type} ) {
            push( @{ $iprotein->{features} }, $iprotein_feature );
          }
          else {
            $self->log()
              ->warn( 'Could not create protein feature from InterPro xref ' .
                      $xref->{primaryIdentifier} . ' as no logic key matches ' .
                      $xref->{quarternaryIdentifier} );
          }
        }
      } ## end if ( uc $xref->{databaseReferenceType...})
    } ## end foreach my $xref ( @{ $iprotein...})
  } ## end foreach my $iprotein ( values...)
  return;
} ## end sub add_interpro_features

sub add_transcripts {
  my ( $self, $data_dir, $iproteinid2protein ) = @_;

  # transcript ID -> transcript
  my $itranscriptid2transcript =
    $self->add_transcripts_from_file( $data_dir . 'transcript',
                                      BIOTYPES()->{PROTEIN_CODING_GENE_TYPE},
                                      $iproteinid2protein );

  # Set locations for each Integr8 transcript object.
  $self->log()->debug("LOAD INTEGR8 TRANSCRIPT LOCATIONS");
  $self->set_object_locations( $itranscriptid2transcript,
                               from_json_file_default(
                                                $data_dir . 'transcriptlocation'
                               ),
                               {},
                               {} );
  $self->log()->debug("LOAD INTEGR8 TRANSCRIPT XREFS");

  # Set xrefs for each Integr8 transcript object.
  $self->set_object_xrefs( $itranscriptid2transcript,
                           from_json_file_default( $data_dir . 'transcriptxref'
                           ) );

  $self->add_operons( $data_dir, $itranscriptid2transcript );

  return;
} ## end sub add_transcripts

sub add_transcripts_from_file {
  my ( $self, $filename, $biotype, $iproteinid2protein ) = @_;
  my %itranscriptid2transcript;
  my $t = from_json_file_default($filename);
  while ( my ( $iprotein_id, $itranscripts ) = each( %{$t} ) ) {
    my $iprotein = $iproteinid2protein->{$iprotein_id};
    foreach my $itranscript ( @{$itranscripts} ) {
      my $existing_transcript =
        $itranscriptid2transcript{ $itranscript->{persistableId} };
      if ( defined $existing_transcript ) {

        # We have processed this transcript before.
        push( @{ $existing_transcript->{proteins} }, $iprotein );
        push( @{ $iprotein->{transcripts} },         $existing_transcript );
      }
      else {

        # We haven't processed this transcript before.
        $itranscript->{biotype} = $biotype;
        push( @{ $itranscript->{proteins} }, $iprotein );
        push( @{ $iprotein->{transcripts} }, $itranscript );
        $itranscriptid2transcript{ $itranscript->{persistableId} } =
          $itranscript;
      }
    }
  }
  return \%itranscriptid2transcript;
} ## end sub add_transcripts_from_file

sub add_operons {
  my ( $self, $data_dir, $itranscriptid2transcript ) = @_;
  $self->log()->debug("LOAD INTEGR8 OPERONS");
  my $ioperonid2operon =
    $self->add_operons_from_file( $data_dir . 'operon',
                                  $itranscriptid2transcript );

  # Set locations for each Integr8 transcript object.
  $self->log()->debug("LOAD INTEGR8 OPERON LOCATIONS");
  $self->set_object_locations( $ioperonid2operon,
                               from_json_file_default(
                                                    $data_dir . 'operonlocation'
                               ),
                               {},
                               {} );
  $self->log()->debug("LOAD INTEGR8 OPERON XREFS");

  # Set xrefs for each Integr8 transcript object.
  $self->set_object_xrefs( $ioperonid2operon,
                           from_json_file_default( $data_dir . 'operonxref' ) );
  return;
}

sub add_operons_from_file {
  my ( $self, $filename, $itranscriptid2transcript ) = @_;
  my %ioperonid2operon;
  my $o = from_json_file_default($filename);
  while ( my ( $itranscript_id, $ioperons ) = each( %{$o} ) ) {
    foreach my $ioperon ( @{$ioperons} ) {
      $self->log()
        ->debug( "operon " . $ioperon->{persistableId} . " for transcript" .
                 $itranscript_id );
      my $itranscript     = $itranscriptid2transcript->{$itranscript_id};
      my $existing_operon = $ioperonid2operon{ $ioperon->{persistableId} };
      if ( !defined $existing_operon ) {
        $existing_operon = $ioperon;
        $ioperonid2operon{ $ioperon->{persistableId} } = $ioperon;
      }
      push( @{ $existing_operon->{transcripts} }, $itranscript );
      $itranscript->{operon} = $existing_operon;
    }
  }
  return \%ioperonid2operon;
}

sub add_pseudogenes {
  my ( $self, $data_dir, $componentsById ) = @_;

  # Pseudo genes.
  $self->log()->info("LOAD PSEUDOGENES");

  # pseudogene ID -> pseudogene
  my $ipgeneid2pgene =
    $self->add_genes_from_file( $data_dir . 'pseudogene',
                                BIOTYPES()->{PSEUDOGENE_TYPE},
                                $componentsById );
  $self->log()->info("LOAD INTEGR8 PSEUDOGENE NAMES");
  $self->add_gene_names( $data_dir . 'pseudogenename', $ipgeneid2pgene );
  $self->log()->info("LOAD INTEGR8 PSEUDOGENE LOCATIONS");

  # Set locations for each Integr8 pseudogene object.
  $self->set_object_locations( $ipgeneid2pgene,
                               from_json_file_default(
                                                $data_dir . 'pseudogenelocation'
                               ),
                               {},
                               {} );
  $self->log()->info("LOAD INTEGR8 PSEUDOGENE XREFS");

  # Set xrefs for each Integr8 pseudogene object.
  $self->set_object_xrefs( $ipgeneid2pgene, {} );
  $self->log()->info("REMOVE DUPLICATE INTEGR8 PSEUDOGENES");
  $self->remove_duplicate_genes_from_hash($ipgeneid2pgene);
  $self->log()->info("PSEUDOGENES - COMPLETED");
  return $ipgeneid2pgene;
} ## end sub add_pseudogenes

sub add_genes_from_file {
  my ( $self, $filename, $biotype, $componentsById ) = @_;
  my $obj = from_json_file_default($filename);
  my $igeneid2gene;
  while ( my ( $component_id, $igenes ) = each( %{$obj} ) ) {
    my $component = $componentsById->{$component_id};
    foreach my $igene ( @{$igenes} ) {
      $igene->{biotype}                                        = $biotype;
      $igene->{component_id}                                   = $component_id;
      $igeneid2gene->{ $igene->{ NAMES()->{PERSISTABLE_ID} } } = $igene;
    }
    push @{ $component->{genes} }, @{$igenes};
  }
  return $igeneid2gene;
}

sub add_gene_names {
  my ( $self, $filename, $igeneid2gene ) = @_;
  my %igeneid2name;
  my $obj = from_json_file_default($filename);
  while ( my ( $igene_id, $igene_names ) = each( %{$obj} ) ) {
    foreach my $igene_name ( @{$igene_names} ) {
      push( @{ $igeneid2gene->{$igene_id}->{names}->{ $igene_name->{type} } },
            $igene_name->{name} );
    }
  }
  return;
}

sub add_rna_gene_names {
  my ( $self, $igene ) = @_;

  # convert nameMap hash into a 'names' hash
  # so we can reuse add_synonyms method (in DisplayXrefFinder)

  my %names;
  my $igene_nameMap_href = $igene->{nameMap};

  foreach my $igene_type ( keys(%$igene_nameMap_href) ) {
    foreach my $name_entry_href ( @{ $igene_nameMap_href->{$igene_type} } ) {
      if ( !defined $names{ $name_entry_href->{type} } ) {
        $names{ $name_entry_href->{type} } = [ $name_entry_href->{name} ];
      }
      else {
        my $names_aref = $names{ $name_entry_href->{type} };
        push( @$names_aref, $name_entry_href->{name} );
      }
    }
  }

  $igene->{names} = \%names;

  return;
} ## end sub add_rna_gene_names

#sub add_gene_descriptions {
#	my ( $filename, $igeneid2gene ) = @_;
#	my $obj = from_json_file_default($filename);
#	while ( my ( $igene_id, $description ) = each( %{$obj} ) ) {
#		$igeneid2gene->{$igene_id}->{description} = $description;
#	}
#	return;
#}

sub add_rnagenes {
  my ( $self, $filename, $transcript_filename, $componentsById ) = @_;

  $self->log()->info("LOAD NON-CODING RNA GENES");
  my $obj = from_json_file_default($filename);
  my $igs = {};
  while ( my ( $component_id, $igenes ) = each( %{$obj} ) ) {
    my @irnagene;
    foreach my $igene ( @{$igenes} ) {
      $igene->{component_id} = $component_id;
      $igene->{locations}    = [ $igene->{location} ];
      delete $igene->{location};
      if ( $self->config()->{suppressTrackingRefs} == 1 ) {
        $igene->{xrefs} = [
          grep {
            $_->{databaseReferenceType}{ensemblName} !~ m/ENA_FEATURE_[A-Z]+/
          } @{ $igene->{databaseReferences} } ];
      }
      else {
        $igene->{xrefs} = $igene->{databaseReferences};
      }
      delete $igene->{databaseReferences};
      foreach my $xref ( @{ $igene->{xrefs} } ) {
        $xref->{object} = $igene;
      }
      $self->add_rna_gene_names($igene);
      push( @irnagene, $igene );
    }
    @irnagene = $self->remove_duplicate_genes_from_array( \@irnagene );
    foreach my $ig (@irnagene) {
      $igs->{ $ig->{ NAMES()->{PERSISTABLE_ID} } } = $ig;
    }
    push @{ $componentsById->{$component_id}{genes} }, @irnagene;
  } ## end while ( my ( $component_id...))
  $self->add_rna_transcripts( $transcript_filename, $igs );
  return;
} ## end sub add_rnagenes

sub add_rna_transcripts {
  my ( $self, $transcript_filename, $igenes ) = @_;
  my $obj = from_json_file_default($transcript_filename);
  while ( my ( $gene_id, $itranscripts ) = each( %{$obj} ) ) {
    foreach my $itranscript ( @{$itranscripts} ) {
      $itranscript->{locations} = [ $itranscript->{location} ];
      delete $itranscript->{location};
      if ( $self->config()->{suppressTrackingRefs} == 1 ) {
        $itranscript->{xrefs} = [
          grep {
            $_->{databaseReferenceType}{ensemblName} !~ m/ENA_FEATURE_[A-Z]+/
          } @{ $itranscript->{databaseReferences} } ];
      }
      else {
        $itranscript->{xrefs} = $itranscript->{databaseReferences};
      }
      delete $itranscript->{databaseReferences};
      foreach my $xref ( @{ $itranscript->{xrefs} } ) {
        $xref->{object} = $itranscript;
      }
      push @{ $igenes->{$gene_id}->{transcripts} }, $itranscript;
    }
  }
  return;
} ## end sub add_rna_transcripts

sub remove_duplicate_genes_from_array {
  my ( $self, $igenes ) = @_;

  # Gene location ID hash.
  # Gene location ID = start-end-strand
  my %igene_location_ids;
  my @igenes_unique;
  my $i = 0;
  foreach my $igene ( @{$igenes} ) {
    my $igene_location = $igene->{locations}[0];
    my $igene_location_id =
      $igene_location->{min} . '|' . $igene_location->{max} . '|' .
      $igene_location->{strand};
    if ( exists $igene_location_ids{$igene_location_id} ) {
      delete @{$igenes}[$i];
      $self->log()
        ->info( 'Removed duplicate gene: ', $igene_location_id, "\n" );
    }
    else {
      $igene_location_ids{$igene_location_id} = undef;
      push( @igenes_unique, $igene );
    }
    $i++;
  }
  return @igenes_unique;
} ## end sub remove_duplicate_genes_from_array

sub remove_duplicate_genes_from_hash {
  my ( $self, $igeneid2gene ) = @_;

  # Gene location ID hash.
  # Gene location ID = start-end-strand
  my %igene_location_ids;
  foreach my $igene ( values( %{$igeneid2gene} ) ) {
    my $igene_location = $igene->{locations}[0];
    my $igene_location_id =
      $igene_location->{min} . '|' . $igene_location->{max} . '|' .
      $igene_location->{strand} . '|' . $igene_location->{state};
    if ( exists $igene_location_ids{$igene_location_id} ) {
      delete $igeneid2gene->{ $igene->{persistableId} };
      $self->log()
        ->debug( 'Removed duplicate gene: ' . $igene->{persistableId} );
    }
    else {
      $igene_location_ids{$igene_location_id} = undef;
    }
  }
  return;
}

sub set_object_xrefs {
  my ( $self, $iobjectid2object_ref, $iobjectid2xrefs_ref ) = @_;
  while ( my ( $iobject_id, $ixrefs ) = each( %{$iobjectid2xrefs_ref} ) ) {
    my $iobject = $iobjectid2object_ref->{$iobject_id};
    if ( $self->config()->{suppressTrackingRefs} == 1 ) {
      $iobject->{xrefs} = [
        grep {
          $_->{databaseReferenceType}{ensemblName} !~ m/ENA_FEATURE_[A-Z]+/
        } @{$ixrefs} ];
    }
    else {
      $iobject->{xrefs} = $ixrefs;
    }
    my $xrefs = [];
    foreach my $ixref ( @{ $iobject->{xrefs} } ) {
      next if !defined $ixref->{databaseReferenceType}{ensemblName};

      # description
      my $key =
        $ixref->{databaseReferenceType}{ensemblName} . ":" .
        $ixref->{primaryIdentifier};
      my $val = $xref_details->{description}{$key};
      if ( defined $val ) {
        if ( !defined $ixref->{description} || $ixref->{description} eq '' ) {
          $ixref->{description} = $val;
        }
        elsif ( $val ne $ixref->{description} ) {
          $self->log()
            ->warn( "Conflicting descriptions found for $key: $val vs. " .
                    $ixref->{description} );
        }
      }
      elsif ( defined $ixref->{description} && $ixref->{description} ne '' ) {
        $xref_details->{description}{$key} = $ixref->{description};
      }
      $ixref->{object} = $iobject;
      if ( defined $ixref->{version} ) {
        if ( looks_like_number( $ixref->{version} ) && $ixref->{version} == 0 )
        {
          $ixref->{version} = undef;
        }
        elsif ( $ixref->{version} eq '' ) {
          $ixref->{version} = undef;
        }
      }
      push @$xrefs, $ixref;
    } ## end foreach my $ixref ( @{ $iobject...})
    $iobject->{xrefs} = $xrefs;
  } ## end while ( my ( $iobject_id,...))
  return;
} ## end sub set_object_xrefs

sub set_object_locations {
  my ( $self, $iobjectid2object, $iobjectid2locations,
       $ilocationid2sublocations, $ilocationid2locationmods )
    = @_;
  my $PERSISTABLE_ID = NAMES()->{PERSISTABLE_ID};
  while ( my ( $iobject_id, $ilocations ) = each( %{$iobjectid2locations} ) ) {
    my $iobject = $iobjectid2object->{$iobject_id};
    $iobject->{locations} = $ilocations;
    foreach my $ilocation ( @{$ilocations} ) {
      $ilocation->{object} = $iobject;
      my $isublocations =
        $ilocationid2sublocations->{ $ilocation->{$PERSISTABLE_ID} };
      if ( defined $isublocations && scalar @$isublocations ) {
        $ilocation->{sublocations} = $isublocations;
      }
      if ( $ilocation->{sublocations} &&
           scalar( @{ $ilocation->{sublocations} } ) )
      {
        foreach my $isublocation ( @{ $ilocation->{sublocations} } ) {
          $isublocation->{location} = $ilocation;
        }
      }
      my $ilocationmods =
        $ilocationid2locationmods->{ $ilocation->{$PERSISTABLE_ID} } || [];
      $ilocation->{mods} = $ilocationmods;
      foreach my $ilocationmod ( @{$ilocationmods} ) {
        $ilocationmod->{location} = $ilocation;
      }
    }
  } ## end while ( my ( $iobject_id,...))
  return;
} ## end sub set_object_locations
1;
__END__

=head1 NAME

GenomeLoader::Parser - functions for parsing a set of json dumps into ensembl objects

=head1 SYNOPSIS

TODO functions for parsing a set of json dumps into ensembl objects

=head1 AUTHORS

Dan Staines <dstaines@ebi.ac.uk>
Alan Horne <hornead@ebi.ac.uk>

=head1 METHODS
=head2 parse

 Title       : parse
 Description : parses Integr8 component JSON dumper files into Ensembl objects
 Args        : species_id, directory for dumper files
 Returns     : nothing

=head2 add_genes
  Title      : add_genes
  Description: Loads protein coding genes or pseudogenes from a file into %igeneid2gene
  Args[1]    : filename
  Args[2]    : biotype
  Returns    : ref to %igeneid2gene

