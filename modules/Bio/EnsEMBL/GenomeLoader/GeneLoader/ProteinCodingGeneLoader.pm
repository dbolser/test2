
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
# $Date$
# $Author$
#
# Object for loading data into an ensembl database
#
use warnings;
use strict;

package Bio::EnsEMBL::GenomeLoader::GeneLoader::ProteinCodingGeneLoader;
use base qw(Bio::EnsEMBL::GenomeLoader::GeneLoader);

use Carp;
use Data::Dumper;
use Bio::EnsEMBL::GenomeLoader::Constants
  qw(XREFS NAMES PEPSTATS_CODES BIOTYPES);
use Bio::EnsEMBL::Attribute;
use Bio::EnsEMBL::Gene;
use Bio::EnsEMBL::ProteinFeature;
use Bio::EnsEMBL::Transcript;
use Bio::EnsEMBL::Translation;
use Bio::EnsEMBL::Exon;
use Bio::EnsEMBL::SeqEdit;
use Bio::EnsEMBL::DBEntry;
use Bio::EnsEMBL::Operon;
use Bio::EnsEMBL::OperonTranscript;
use Bio::EnsEMBL::Utils::Exception qw(throw);
use Bio::EnsEMBL::GenomeLoader::AnalysisFinder qw/get_analysis_by_name/;
use Digest::MD5;
use IPC::Open2;

sub new {
  my $caller = shift;
  my $class  = ref($caller) || $caller;
  my $self   = $class->SUPER::new(@_);
  $self->{attrs}{INIT_MET} =
    Bio::EnsEMBL::Attribute->new(
               -CODE        => 'initial_met',
               -NAME        => 'Starts with methionine',
               -DESCRIPTION => 'Start codon encodes methionine (bacteria only)',
               -VALUE       => '1 1 M' );
  $self->{attrs}{HAS_START_CODON} =
    Bio::EnsEMBL::Attribute->new(
                           -CODE        => 'has_start_codon',
                           -NAME        => 'Contains start codon',
                           -DESCRIPTION => "Contains start codon (5' complete)",
                           -VALUE       => '1 1 M' );
  $self->{attrs}{HAS_STOP_CODON} =
    Bio::EnsEMBL::Attribute->new(
                            -CODE        => 'has_stop_codon',
                            -NAME        => "Contains stop codon",
                            -DESCRIPTION => "Contains stop codon (3' complete)",
                            -VALUE       => 1 );

  # create an operon cache
  $self->{_operons}            = {};
  $self->{_operon_transcripts} = {};

  return $self;
} ## end sub new

sub load_gene {
  my ( $self, $igene, $slice ) = @_;
  my $time;
  my $assembly_exception_adaptor =
    $self->dba()->get_AssemblyExceptionFeatureAdaptor();
  my $ctx = Digest::MD5->new();

  # Remove swissprot and trembl xrefs from the gene.
  foreach my $xref ( @{ $igene->{xrefs} } ) {
    my $ensembl_dbname = $xref->{databaseReferenceType}{ensemblName};
    if ( $ensembl_dbname eq XREFS()->{UNIPROT_SWISSPROT} ||
         $ensembl_dbname eq XREFS()->{UNIPROT_TREMBL} )
    {
      $xref->{suppress} = 1;
    }
  }

  # Create gene.
  $time = time();

# rule for biotype - all pseudogenes -> pseudogene, at least one protein_coding -> protein_coding
  my $nProteins = scalar @{ $igene->{proteins} };
  my $nPseudo = scalar grep { $_->{pseudo} } @{ $igene->{proteins} };
  my $biotype =
    ( $nPseudo == $nProteins ) ? BIOTYPES()->{PSEUDOGENE_TYPE} :
                                 BIOTYPES()->{PROTEIN_CODING_GENE_TYPE};

  my $egene = Bio::EnsEMBL::Gene->new( -SLICE         => $slice,
                                       -BIOTYPE       => $biotype,
                                       -SOURCE        => $self->{source},
                                       -ANALYSIS      => $self->{gene_analysis},
                                       -CREATED_DATE  => $time,
                                       -MODIFIED_DATE => $time );

  $self->set_stable_id( $igene, $egene );

  # Set gene xrefs (including GO xrefs).
  $self->set_xrefs( $igene, $egene );

  # Set gene display xref.
  $self->set_display_xref( $igene, $egene );
  my $operon_transcripts = {};

  $self->store_proteins( $egene, $igene, $operon_transcripts );
  $self->set_description( $egene, $igene );
  if ( !$egene->display_xref() ) {
    $self->set_display_xref( $igene, $egene );
  }

  # Store the gene, but only if it has at least one transcript.
  my $ts = $egene->get_all_Transcripts();
  if ( $ts && @{$ts} ) {
    eval {
      $self->log()
        ->debug( "Storing protein_coding gene " . $igene->{identifyingId} );

      # store gene
      my $transcriptN = 0;
      for my $t (@$ts) {
        if ( !defined $t->display_xref() ) {
          $self->log->debug(
             "Attempting to set transcript.display_xref now translation added");

          # may wish to use protein ID again
          $self->set_display_xref( {}, $t, $egene, ++$transcriptN );
        }
      }
      $self->dba()->get_GeneAdaptor->store( $egene, undef, 1 );
      my $translationCnt     = 0;
      my $translationFailCnt = 0;
      foreach my $t ( @{$ts} ) {
        my $tt = $t->translation();
        if ($tt) {
          $translationCnt++;
          eval {
            my $seq = $tt->seq();
            $ctx->add($seq);
            if ( $seq =~ /\*/ ) {
              throw( "Translation " . $tt->stable_id() . "(" . $tt->dbID() .
                     ") contains a stop codon" );
            }
          };
          if ($@) {
            $self->log()
              ->warn(
                 "Could not translate protein " . $tt->stable_id() . ":" . $@ );

            # change biotype to nontranslating_cds
            $self->dba()->dbc()->sql_helper()
              ->execute_update(
"update transcript set biotype='nontranslating_cds' where transcript_id="
                . $t->dbID() );
            $translationFailCnt++;
          }
        }
        for my $altt ( @{ $t->get_all_alternative_translations() } ) {
          my $seq = $altt->seq();
          $ctx->add($seq);
          if ( $seq =~ /\*/ ) {
            $self->log()
              ->warn(
                    "Translation " . $altt->dbID() . " contains a stop codon" );
          }
        }

      } ## end foreach my $t ( @{$ts} )
      if ( $translationFailCnt > 0 && $translationFailCnt == $translationCnt ) {
        $self->dba()->dbc()->sql_helper()
          ->execute_update(
                 "update gene set biotype='nontranslating_cds' where gene_id=" .
                   $egene->dbID() );
      }
    };
    if ($@) {
      croak( $self->store_gene_error_handler( $igene, $egene ) );
      exit;
    }
    for my $operon_transcript ( values %$operon_transcripts ) {
      $self->log()
        ->debug( "Storing gene " . $egene->dbID() . " on operon-transcript " .
                 $operon_transcript->dbID() );
      $self->dba()->get_OperonTranscriptAdaptor()
        ->store_genes_on_OperonTranscript( $operon_transcript, [$egene] );
    }
  } ## end if ( $ts && @{$ts} )
  else {
    croak( "Cannot store gene " . $igene->{persistableId} .
           " as it has no transcripts" );
  }
  return $ctx->hexdigest();
} ## end sub load_gene

sub set_description {
  my ( $self, $egene, $igene ) = @_;

  # Gene description.
  if ( $igene->{description} ) {
    $egene->description( $igene->{description} .
         $self->{displayXrefFinder}->get_description_source( $egene, $igene ) );
  }
}

sub get_operon {
  my ( $self, $ioperon, $slice, $analysis ) = @_;

  # look up operon in cache
  my $operon = $self->{_operons}{ $ioperon->{name} };
  if ( !defined $operon ) {
    $operon = Bio::EnsEMBL::Operon->new(
                                      -START  => $ioperon->{location}->{min},
                                      -END    => $ioperon->{location}->{max},
                                      -STRAND => $ioperon->{location}->{strand},
                                      -SLICE  => $slice,
                                      -DISPLAY_LABEL => $ioperon->{name},
                                      -ANALYSIS      => $analysis );
    $self->set_xrefs( $ioperon, $operon );
    $self->dba()->get_OperonAdaptor()->store($operon);
    $self->{_operons}{ $ioperon->{name} } = $operon;
  }
  return $operon;
}

sub get_operon_transcript {
  my ( $self, $operon, $itranscript, $analysis ) = @_;
  my $operon_transcript = $self->{_operon_transcripts}{ $itranscript->{name} };
  if ( !defined $operon_transcript ) {
    $operon_transcript =
      Bio::EnsEMBL::OperonTranscript->new(
                                  -START  => $itranscript->{location}->{min},
                                  -END    => $itranscript->{location}->{max},
                                  -STRAND => $itranscript->{location}->{strand},
                                  -SLICE  => $operon->slice(),
                                  -DISPLAY_LABEL => $itranscript->{name},
                                  -ANALYSIS      => $analysis );
    my $operon_refs    = [];
    my $nonoperon_refs = [];
    for my $xref ( @{ $itranscript->{xrefs} } ) {
      if ( $xref->{databaseReferenceType}{ensemblName} =~ m/Regulon.*/ ) {
        push @$operon_refs, $xref;
      }
      else {
        push @$nonoperon_refs, $xref;
      }
    }
    $itranscript->{xrefs} = $nonoperon_refs;
    $self->set_xrefs_from_list( $operon_refs, $operon_transcript );
    $self->dba()->get_OperonTranscriptAdaptor()
      ->store( $operon_transcript, $operon->dbID() );
    $operon->add_OperonTranscript($operon_transcript);
    $self->{_operon_transcripts}{ $itranscript->{name} } = $operon_transcript;
  } ## end if ( !defined $operon_transcript)
  return $operon_transcript;
} ## end sub get_operon_transcript

sub get_transcript {

  my ( $self, $egene, $itranscript, $iprotein, $transcriptN ) = @_;

  # Create exons.
  my @eexons = $self->get_exons( $iprotein, $egene->slice() );
  $self->log()
    ->debug( "Got " .
          scalar @eexons . " exons for protein " . $iprotein->{identifyingId} );

  # Don't store the gene unless we have some exons.
  croak "No exons found for gene " . $egene->stable_id() unless @eexons;

# If necessary, stretch the first and last exon to fit the boundary of the Integr8 transcript.
# Adjust the translation seq start and end accordingly to allow for any UTRs created.
  my $itranscript_start = $itranscript->{location}->{min};
  my $eexon_s           = $eexons[0];
  my $diff_start        = $eexon_s->start() - $itranscript_start;
  if ( $diff_start > 0 ) {
    $self->log()->debug( "exon ($eexon_s) = " . $egene->stable_id() .
                           ' start changed from ' . $eexon_s->start() .
                           ' to itranscript_start=' . $itranscript_start .
                           ', diff_start=',
                         $diff_start );
    $eexon_s->start($itranscript_start);
  }
  my $eexon_e         = $eexons[-1];
  my $itranscript_end = $itranscript->{location}->{max};
  my $diff_end        = $itranscript_end - $eexon_e->end();
  if ( $diff_end > 0 ) {
    $self->log()
      ->debug( "exon ($eexon_e) " . $egene->stable_id() . ' end changed from ' .
               $eexon_e->end() . ' to itranscript_end = ' . $itranscript_end .
               ' diff_end = ' . $diff_end );
    $eexon_e->end($itranscript_end);
  }

  # Create transcript.
  my $time = time();
  my $biotype =
    ( $iprotein->{pseudo} ) ? BIOTYPES()->{PSEUDOGENE_TYPE} :
                              BIOTYPES()->{PROTEIN_CODE_GENE_TYPE};
  my $etranscript =
    Bio::EnsEMBL::Transcript->new( -BIOTYPE       => $biotype,
                                   -VERSION       => undef,
                                   -SLICE         => $egene->slice(),
                                   -ANALYSIS      => $egene->analysis(),
                                   -SOURCE        => $self->{source},
                                   -CREATED_DATE  => $time,
                                   -MODIFIED_DATE => $time );

  # Set transcript xrefs (including GO xrefs).
  $self->set_xrefs( $itranscript, $etranscript );

  # Set transcript display xref.
  $self->set_stable_id( $itranscript, $etranscript, $egene, $transcriptN );
  $self->set_display_xref( $itranscript, $etranscript, $egene, $transcriptN );

  # If reverse strand then reverse the exons
  # so that the ensembl rank is applied instead of the Integr8 rank.
  my @sorted_eexons = @eexons;
  my $strand        = $itranscript->{location}->{strand};
  if ( $strand == -1 ) {
    @sorted_eexons = reverse @sorted_eexons;
  }
  my $i = 0;
  foreach my $eexon (@sorted_eexons) {
    $i++;
    $etranscript->add_Exon( $eexon, $i )
      ;    # mod to allow specific ordering of transcripts
    $self->set_stable_id( $iprotein, $eexon, $etranscript, $i );
  }
  $self->recalculate_coordinates($etranscript)
    ;      # mod to allow specific ordering of transcripts

  #				# correct coordinates this time
  #				$etranscript->start($sorted_eexons[0]->start());
  #				$etranscript->end($sorted_eexons[-1]->end());

  $self->set_frameshift_attributes($etranscript);

  # Add transcript to gene.
  $egene->add_Transcript($etranscript);

  # keep track of transcripts
  return {

    #		diff_start => $diff_start,
    #		diff_end   => $diff_end,
    transcript => $etranscript,
    eexons     => \@eexons };
} ## end sub get_transcript

sub recalculate_coordinates {
  my ( $self, $transcript ) = @_;

  my $exons = $transcript->get_all_Exons();

  my $first_exon = $exons->[0];
  my $last_exon  = $exons->[-1];

  # make some assumptions about no empty exons
  my $strand = $first_exon->strand();
  my $slice  = $first_exon->slice();

  # quick check for transplicing
  for my $exon (@$exons) {
    if ( $strand != $exon->strand() ) {
      $self->log()->warn("Transcript contained trans splicing event");
    }
    if ( defined($slice) &&
         $exon->slice() &&
         $exon->slice()->name() ne $slice->name() )
    {
      throw(
         "Exons with different slices " . "are not allowed on one Transcript" );
    }
  }
  $transcript->strand($strand);
  $transcript->slice($slice);

  # if positive strand
  if ( $strand == 1 ) {
    $transcript->start( $first_exon->start() );
    $transcript->end( $last_exon->end() );
  }
  else {
    $transcript->start( $last_exon->start() );
    $transcript->end( $first_exon->end() );
  }

  # flush cached internal values that depend on the exon coords
  $transcript->{'transcript_mapper'}   = undef;
  $transcript->{'coding_region_start'} = undef;
  $transcript->{'coding_region_end'}   = undef;
  $transcript->{'cdna_coding_start'}   = undef;
  $transcript->{'cdna_coding_end'}     = undef;
  return;
} ## end sub recalculate_coordinates

sub store_proteins {
  my ( $self, $egene, $igene, $operon_transcripts ) = @_;
  $self->log()
    ->debug(
          "Storing proteins for gene '" . ( $egene->stable_id() || "unknown" ) .
            "'" );

  # - need to flip the relationship around so we get transcript->protein map
  my $transcripts = {};
  foreach my $iprotein ( @{ $igene->{proteins} } ) {
    foreach my $itranscript ( @{ $iprotein->{transcripts} } ) {
      push @{ $itranscript->{proteins} }, $iprotein;
      $transcripts->{ $itranscript->{identifyingId} } = $itranscript;
    }
  }

  # work over each set of transcript-proteins
  my $transcriptN = 0;
  foreach my $itranscript ( values $transcripts ) {
    my @iproteins =
      sort { $self->get_cds_length($b) <=> $self->get_cds_length($a) }
      @{ $itranscript->{proteins} };
    $self->log->debug(
           "Processing transcript " . $itranscript->{identifyingId} . " with " .
             scalar(@iproteins) . " proteins" );
    my $can_iprotein = shift(@iproteins);

    # deal with operons
    my $ioperon = $itranscript->{operon};
    if ( defined $ioperon ) {
      $self->log()
        ->debug( "Attaching transcript to operon " . $ioperon->{identifyingId} .
                 " for " . $egene->stable_id() );

      # create an operon
      my $operon;
      eval {
        $operon =
          $self->get_operon( $ioperon, $egene->slice(), $egene->analysis() );
      };
      if ($@) {
        $self->log()
          ->warn(
            "Could not create operon " . $ioperon->{identifyingId} . ":" . $@ );
      }
      else {

        # create an operon transcript
        my $operon_transcript =
          $self->get_operon_transcript( $operon, $itranscript,
                                        $egene->analysis() );
        $self->log()
          ->debug( "Got operon transcript " . $operon_transcript->dbID() );

        # add the operon transcript to the list to store this gene on
        $operon_transcripts->{ $operon_transcript->dbID() } =
          $operon_transcript;

        # reset the transcript locations to those of the canonical
        $itranscript->{location} = $can_iprotein->{location};
      }
    } ## end if ( defined $ioperon )

    my $strand = $can_iprotein->{location}->{strand};

    # get exons and transcript based on canonical protein
    $self->log()
      ->debug(
         "Handling transcript " . $itranscript->{identifyingId} . " for gene " .
           $igene->{identifyingId} );
    my $et = $self->get_transcript( $egene,        $itranscript,
                                    $can_iprotein, ++$transcriptN );
    my $etranscript = $et->{transcript};

    if ( !$can_iprotein->{pseudo} ) {
      $etranscript->translation(
        $self->get_translation( $can_iprotein, $et->{eexons},
                                $etranscript,  $itranscript ) );
    }

    foreach my $iprotein (@iproteins) {
      if ( !$iprotein->{pseudo} ) {
        $self->log->debug(
                   "Processing alt translation " . $iprotein->{identifyingId} );

        $etranscript->add_alternative_translation(
                              $self->get_translation( $iprotein, $et->{eexons},
                                                      $etranscript, $itranscript
                              ) );
      }
    }
  } ## end foreach my $itranscript ( values...)
  return;
} ## end sub store_proteins

sub get_cds_length {
  my ( $self, $iprotein ) = @_;
  my $plen = 0;

# If a location has sublocations then use them to create exons, otherwise use the location.
  my $ilocations;
  if ( defined $iprotein->{location}->{sublocations} &&
       @{ $iprotein->{location}->{sublocations} } > 0 )
  {
    $ilocations = $iprotein->{location}->{sublocations};

# Sort by Integr8 (EMBL) rank.
# This means that exons are ordered by location ascending value, regardless of strand.
# Note: ensembl rank is the order in which exons are used in the translation.
# (ie. ensembl rank is opposite to Integr8 rank for reverse strand).
    my @sorted = sort { $a->{rank} <=> $b->{rank} } @{$ilocations};
    $ilocations = \@sorted;
  }
  else {
    $ilocations = [ $iprotein->{location} ];
  }
  foreach my $ilocation ( @{$ilocations} ) {
    $plen += $ilocation->{max} - $ilocation->{min} + 1;
  }
  foreach my $imod ( @{ $iprotein->{location}->{mods} } ) {
    if ( $imod->{stop} == $imod->{start} ) {
      $plen += length $imod->{proteinSeq};
    }
  }
  return $plen;
} ## end sub get_cds_length

sub get_translation {
  my ( $self, $iprotein, $eexons, $etranscript, $itranscript ) = @_;
  $self->log()
    ->debug( "Getting translation for protein " . $iprotein->{identifyingId} .
             " (pseudo " . $iprotein->{pseudo} . ")" );

  my $strand = $iprotein->{location}->{strand};
  my $frame  = $iprotein->{codonStart} - 1;

  # Create translation, one per transcript.
  # For translation, choose start, end exon, seq_start and seq_end accordingly !
  my $start_exon;
  my $end_exon;
  my $seq_start;
  my $seq_end;

  my $protein_start = $iprotein->{location}{min};
  my $protein_end   = $iprotein->{location}{max};

  for my $exon (@$eexons) {
    if ( $exon->start() > $exon->end() ) {
      if ( $exon->start() >= $protein_start && $exon->end() <= $protein_start )
      {
        $start_exon = $exon;
      }
      if ( $exon->start() >= $protein_end && $exon->end() <= $protein_end ) {
        $end_exon = $exon;
      }
    }
    else {
      if ( $exon->start() <= $protein_start && $exon->end() >= $protein_start )
      {
        $start_exon = $exon;
      }
      if ( $exon->start() <= $protein_end && $exon->end() >= $protein_end ) {
        $end_exon = $exon;
      }
    }
  }

  if ( $strand == -1 ) {

    # Reverse.
    # swap exons
    my $tmp = $end_exon;
    $end_exon   = $start_exon;
    $start_exon = $tmp;

    $seq_start = $start_exon->end() - $protein_end + 1;
    $seq_end = $end_exon->length() - ( $protein_start - $end_exon->start() );

  }
  else {

    $seq_start = $protein_start - $start_exon->start() + 1;
    $seq_end = $end_exon->length() - ( $end_exon->end() - $protein_end );

  }
  $self->log()
    ->debug( "start exon ($start_exon) = " . $start_exon->start . '-' .
             $start_exon->end . ':' . $start_exon->strand );
  $self->log()
    ->debug(
     "end exon ($end_exon) = " . $end_exon->start . '-' . $end_exon->end . ':' .
       $end_exon->strand );
  $seq_start += $frame;
  $self->log()->debug("seq_start = $seq_start");
  $self->log()->debug("seq_end   = $seq_end");
  my $time = time();
  if ( $seq_start < 1 ) {
    croak "Translation start $seq_start cannot be less than 1";
  }
  if ( $seq_end < 1 ) {
    croak "Translation end $seq_end cannot be less than 1";
  }

# the $seq_start and $seq_end values are relative to the exon (start of exon = 1).
  my $etranslation =
    Bio::EnsEMBL::Translation->new( -STABLE_ID    => $iprotein->{persistableId},
                                    -START_EXON   => $start_exon,
                                    -SEQ_START    => $seq_start,
                                    -END_EXON     => $end_exon,
                                    -SEQ_END      => $seq_end,
                                    -CREATED_DATE => $time,
                                    -MODIFIED_DATE => $time );
  $self->set_stable_id( $iprotein, $etranslation );

  # Set exon phases.  Exons must be in Integr8 rank order.
  $self->set_exon_phases( $etranslation, $eexons );

  foreach
    my $attrib ( @{ $self->get_translation_attribs( $iprotein->{location} ) } )
  {
    if ( defined $attrib ) {
      $etranslation->add_Attributes($attrib);
    }
  }
  foreach my $iprotein_locationmod ( @{ $iprotein->{location}->{mods} } ) {
    $etranslation->add_Attributes(
                                 $self->location_mod_to_attribute(
                                   $iprotein->{location}, $iprotein_locationmod,
                                   $iprotein->{codonStart} ) );
  }

  # Set translation xrefs (including GO xrefs).
  $self->set_xrefs( $iprotein, $etranslation );
  ### For each protein feature.
  # Set translation for transcript.
  # TODO ALT_INIT add to list of translations
  $etranslation->transcript($etranscript);

# get the length of the translated protein - tricky when we don't have it stored
  my $tlen = $self->get_cds_length($iprotein)/3;

  # TODO ALT_INIT set as canonical if longer than canonical
  foreach my $iprotein_feature ( @{ $iprotein->{proteinFeatures} } ) {
    my $feature_type =
      get_analysis_by_name( $iprotein_feature->{type}, "domain" );
    if ($feature_type) {
      if ( $iprotein_feature->{end} > $tlen ) {
        $self->log()
          ->warn(
          'Could not create protein feature ' . $iprotein_feature->{id} . '/' .
            $iprotein_feature->{type} . ' as it ends (' .
            $iprotein_feature->{end} . ') beyond the end of the translation (' .
            $tlen . ') of ' . $iprotein->{identifyingId} . ' (' .
            $iprotein->{persistableId} . ')' );
      }
      else {
        my $eprotein_feature =
          Bio::EnsEMBL::ProteinFeature->new(
                                     -START    => $iprotein_feature->{start},
                                     -END      => $iprotein_feature->{end},
                                     -HSTART   => $iprotein_feature->{start},
                                     -HEND     => $iprotein_feature->{end},
                                     -HSEQNAME => $iprotein_feature->{id},
                                     -SCORE    => 100,
                                     -SLICE    => $etranscript->slice(),
                                     -HDESCRIPTION => $iprotein_feature->{name},
                                     -ANALYSIS     => $feature_type );

        $etranslation->add_ProteinFeature($eprotein_feature);
      }
    } ## end if ($feature_type)
    else {
      $self->log()
        ->warn( 'Could not create protein feature from logic_key: ' .
                $iprotein_feature->{logic_key} );
    }
  } ## end foreach my $iprotein_feature...

  return $etranslation;
} ## end sub get_translation

sub set_exon_phases {
  my ( $self, $translation, $exons_ref ) = @_;
  my $found_start = 0;
  my $found_end   = 0;
  my $phase       = 0;
  my @exons       = @{$exons_ref};
  if ( $exons[0]->strand == -1 ) {

# Reverse strand, so put exons in ensembl rank order (in the order used in the translation).
    @exons = reverse @exons;
  }
  foreach my $exon (@exons) {

    # Internal and end exons
    if ( $found_start && !$found_end ) {
      $exon->phase($phase);
      $exon->end_phase( ( $exon->length + $exon->phase ) % 3 );
      $phase = $exon->end_phase;
    }
    if ( $translation->start_Exon == $exon ) {
      my $end_phase =
        ( ( $exon->length - $translation->start + 1 ) + $phase ) % 3;
      $exon->phase($phase);
      $exon->end_phase($end_phase);
      $phase       = $exon->end_phase;
      $found_start = 1;
    }
    if ( $translation->end_Exon == $exon ) {
      $found_end = 1;
    }
  }
  return;
} ## end sub set_exon_phases

sub get_translation_attribs {
  my ( $self, $location, $superregnum ) = @_;

  # Are any locations for this protein fuzzy ? Assume not fuzzy.
  my $has_stop_codon  = $self->{attrs}{HAS_STOP_CODON};
  my $has_start_codon = $self->{attrs}{HAS_START_CODON};

  if ( $location->{strand} == -1 ) {
    if ( $location->{min_fuzzy} ) {
      $has_stop_codon = undef;
    }
    if ( $location->{max_fuzzy} ) {
      $has_start_codon = undef;
    }
  }
  else {
    if ( $location->{min_fuzzy} ) {
      $has_start_codon = undef;
    }
    if ( $location->{max_fuzzy} ) {
      $has_stop_codon = undef;
    }
  }

  my @attrs = ();
  if ( defined $has_start_codon ) {
    push @attrs, $has_start_codon;

    # if bacterial, also has init met
    if ( $self->is_prokaryote() == 1 ) {
      push @attrs, $self->{attrs}{INIT_MET};
    }
  }
  if ( defined $has_stop_codon ) {
    push @attrs, $has_stop_codon;
  }

  return \@attrs;
} ## end sub get_translation_attribs

sub calc_pepstats {

  my ( $self, $translation ) = @_;

  # 1. get sequence from translation
  my $peptide_seq;
  $peptide_seq = $translation->seq;
  return if ( $peptide_seq =~ m/[BZX]/ig );
  if ( $peptide_seq !~ /\n$/ ) { $peptide_seq .= "\n" }
  $peptide_seq =~ s/\*$//;

  # 2. run pepstats
  local ( *Reader, *Writer );
  my $pid = open2( \*Reader, \*Writer,
                   "/sw/arch/pkg/EMBOSS-5.0.0/bin/pepstats -filter" );
  print Writer $peptide_seq;
  close Writer;

  # 3. parse output
  while (<Reader>) {
    chomp;
    if (m/^Molecular weight = (\S+)(\s+)Residues = (\d+).*/) {
      $translation->add_Attributes(
                          Bio::EnsEMBL::Attribute->new(
                            '-code' => PEPSTATS_CODES()->{'Number of residues'},
                            '-name' => 'Number of residues',
                            '-value' => $3 ) );
      $translation->add_Attributes(
                            Bio::EnsEMBL::Attribute->new(
                              '-code' => PEPSTATS_CODES()->{'Molecular weight'},
                              '-name' => 'Molecular weight',
                              '-value' => $1 ) );
    }
    elsif (
       m/^Average(\s+)(\S+)(\s+)(\S+)(\s+)=(\s+)(\S+)(\s+)(\S+)(\s+)=(\s+)(\S+)/
      )
    {
      $translation->add_Attributes(
                         Bio::EnsEMBL::Attribute->new(
                           '-code' => PEPSTATS_CODES()->{'Ave. residue weight'},
                           '-name' => 'Ave. residue weight',
                           '-value' => $7 ) );
      $translation->add_Attributes( Bio::EnsEMBL::Attribute->new(
                                        '-code' => PEPSTATS_CODES()->{'Charge'},
                                        '-name' => 'Charge',
                                        '-value' => $12 ) );
    }
    elsif (m/^Isoelectric(\s+)(\S+)(\s+)=(\s+)(\S+)/) {
      $translation->add_Attributes(
                           Bio::EnsEMBL::Attribute->new(
                             '-code' => PEPSTATS_CODES()->{'Isoelectric point'},
                             '-name' => 'Isoelectric point',
                             '-value' => $5 ) );
    }
    elsif (m/FATAL/) {
      $self->log()->warn("pepstats: $_");
    }
  } ## end while (<Reader>)

  # 4. cleanup
  close Reader;
  waitpid( $pid, 0 );
  return;
} ## end sub calc_pepstats

sub location_mod_to_attribute {
  my ( $self, $iprotein_location, $iprotein_location_mod, $offset ) = @_;
  $self->log()
    ->debug( "Mod coords: " . $iprotein_location_mod->{start} . ", " .
             $iprotein_location_mod->{stop} );
  my ( $pep_start, $pep_end ) =
    $self->convert_dna_2_peptide_coords( $iprotein_location,
                                      $iprotein_location_mod->{start},
                                      $iprotein_location_mod->{stop}, $offset );
  my $attr;
  $self->log()->debug("Edit coords: $pep_start, $pep_end");

  # Proceed if we have defined the start of the seq edit.
  if ( defined($pep_start) ) {

# Coordinates are inclusive and one-based, which means that inserts are unusually represented by a start 1bp higher than the end.
# E.g. start = 1, end = 1 is a replacement of the first base but start = 1, end = 0 is an insert BEFORE the first base.
# We need the insert to take place AFTER the start pep calculated from the qualifier.
# This is the same as an insert BEFORE the end pep calculated from the qualifier.
# Always use $pep_start as $pep_end could be in a gap (ie. insertion is after end of a region).
# Sequence edit start and end.
    my $seq_edit_start;
    my $seq_edit_end;
    if ( exists( $iprotein_location_mod->{aminoAcid} ) ) {

# Exception (one-for-one).
# Coordinates are inclusive and one-based, which means that inserts are unusually represented by a start 1bp higher than the end.
# E.g. start = 1, end = 1 is a replacement of the first base but start = 1, end = 0 is an insert BEFORE the first base.
      $seq_edit_start = $pep_start;
      $seq_edit_end   = $pep_end;
    }
    else {

   # Insertion (zero-to-many to zero-to-many).
   # Calculate $seq_edit_start.  Use $pep_start if defined or $pep_end otherwise
   # (undefined pep coord means that it is not within a coding region)
      if ( defined($pep_start) ) {
        $seq_edit_start = $pep_start + 1;
      }
      elsif ( defined($pep_end) ) {
        $seq_edit_start = $pep_end;
      }
      $seq_edit_end = $seq_edit_start - 1;
    }
    if ( !$seq_edit_start || !$seq_edit_end ) {
      croak(
        "Location mod has no start or end: " . Dumper($iprotein_location_mod) );
    }
    my $seq_edit =
      Bio::EnsEMBL::SeqEdit->new(
                               -CODE        => NAMES()->{AMINO_ACID_SUB},
                               -NAME        => NAMES()->{AMINO_ACID_SUB},
                               -DESCRIPTION => NAMES()->{AMINO_ACID_SUB},
                               -ALT_SEQ => $iprotein_location_mod->{proteinSeq},
                               -START   => $seq_edit_start,
                               -END     => $seq_edit_end );
    if ( defined( $seq_edit->code ) &&
         defined( $seq_edit->name ) &&
         defined( $seq_edit->description ) &&
         defined( $seq_edit->alt_seq ) )
    {
      $attr = $seq_edit->get_Attribute;
    }
    else {
      $self->log()
        ->warn( " Could not convert location mod into seq edit : " .
                Dumper($iprotein_location_mod) );
    }
  } ## end if ( defined($pep_start...))
  if ( !defined $attr ) {
    $self->log()
      ->warn( " Could not convert location mod into seq edit attribute  : " .
              Dumper($iprotein_location_mod) );
  }
  return $attr;
} ## end sub location_mod_to_attribute

sub get_exons {
  my ( $self, $iprotein, $slice ) = @_;
  my @eexons = ();
  my $exonN  = 0;

# If a location has sublocations then use them to create exons, otherwise use the location.
  my $ilocations;
  if ( defined $iprotein->{location}->{sublocations} &&
       @{ $iprotein->{location}->{sublocations} } > 0 )
  {
    $ilocations = $iprotein->{location}->{sublocations};

# Sort by Integr8 (EMBL) rank.
# This means that exons are ordered by location ascending value, regardless of strand.
# Note: ensembl rank is the order in which exons are used in the translation.
# (ie. ensembl rank is opposite to Integr8 rank for reverse strand).
    my @sorted = sort { $a->{rank} <=> $b->{rank} } @{$ilocations};
    $ilocations = \@sorted;
  }
  else {
    $ilocations = [ $iprotein->{location} ];
  }
  foreach my $ilocation ( @{$ilocations} ) {
    my $time = time();
    my $eexon = Bio::EnsEMBL::Exon->new( -SLICE         => $slice,
                                         -START         => $ilocation->{min},
                                         -END           => $ilocation->{max},
                                         -STRAND        => $ilocation->{strand},
                                         -CREATED_DATE  => $time,
                                         -MODIFIED_DATE => $time,
                                         -PHASE         => 0,
                                         -END_PHASE     => 0, );

    push( @eexons, $eexon );
  }

  # Handle the case of a circular genome where a feature crosses the origin.
  @eexons = @{ $self->check_exons( $slice, \@eexons ) };

  return @eexons;
} ## end sub get_exons

sub check_exons {
  my ( $self, $slice, $eexons_ref ) = @_;
  my $handleCircular =
    1;    # switch to turn on handling of origin overlapping features

# Look for two exons separated by the origin (forward strand).
# Bioperl cannot peform a translation in this case and Ensembl cannot display features on circular DNA either.
# So do not create any exons in this case.  This will prevent creation of the transcript.
  my @eexons = @{$eexons_ref};
  return $eexons_ref if ( @eexons < 2 );
  my @new_exons = ();
  my $lastOk    = 1;
  my $nExons    = scalar @eexons;
  for ( my $i = 0; $i < $nExons; $i++ ) {
    if ( $eexons[$i]->end == $slice->length &&
         $i < ( $nExons - 1 ) &&
         $eexons[ $i + 1 ]->start == 1 )
    {
      $self->log()
        ->warn(
          'Exons ' . $eexons[$i]->start() . '-' . $eexons[$i]->end() . ':' .
            $eexons[$i]->strand() . ' and ' . $eexons[ $i + 1 ]->start() . '-' .
            $eexons[ $i + 1 ]->end() . ':' . $eexons[ $i + 1 ]->strand() .
            'are a single exon which crosses the origin' );
      if ($handleCircular) {
        $eexons[$i]->end( $eexons[ $i + 1 ]->end() );
        $eexons[$i]->start( $eexons[$i]->start() );
        $self->log()
          ->warn(
          "Merging exons together into reversed form: " . $eexons[$i]->start() .
            "-" . $eexons[$i]->end() );
        push @new_exons, $eexons[$i];
        $lastOk = 0;
      }
      else {
        $self->log()->warn("Skipping...");
        @new_exons = ();
        last;
      }
    } ## end if ( $eexons[$i]->end ...)
    else {
      if ($lastOk) {
        push @new_exons, $eexons[$i];
      }
      else {
        $self->log->warn(
           "Skipping exon " . $eexons[$i]->start() . "-" . $eexons[$i]->end() );
        $lastOk = 1;
      }
    }
  } ## end for ( my $i = 0; $i < $nExons...)
  return \@new_exons;
} ## end sub check_exons

sub set_frameshift_attributes {
  my ( $self, $etranscript ) = @_;
  my $intron_number = 0;
  foreach my $intron ( @{ $etranscript->get_all_Introns() } ) {
    $intron_number++;

    # only interested in the short ones
    if ( $intron->length() < 6 && $intron->length() != 3 ) {
      $etranscript->add_Attributes(
                              Bio::EnsEMBL::Attribute->new(
                                -CODE        => 'Frameshift',
                                -NAME        => 'Frameshift',
                                -DESCRIPTION => 'Frameshift modelled as intron',
                                -VALUE       => $intron_number ) );
    }
  }
  return $intron_number;
}

sub set_transcript_stats {
  my ( $self, $etranscript, $egene, $fuzzy ) = @_;

  # Do the translation and capture the statistics.
  my $aa_seq;
  my $fuzzy_label = $fuzzy ? '_fuzzy' : '';
  $self->{translation_stats}{attempt}++;
  eval { $aa_seq = $etranscript->translate(); };
  if ($@) {
    my $status_key = 'fail' . $fuzzy_label;
    $self->{translation_stats}{$status_key}++;
    $self->log()->warn( '[' . $egene->stable_id() .
                          "] translate() - $status_key: " . $etranscript->start,
                        '-',
                        $etranscript->end,
                        '(' . $etranscript->strand . '):' . $@ );
  }
  elsif ( !defined($aa_seq) ) {
    my $status_key = 'cannot' . $fuzzy_label;
    $self->{translation_stats}{$status_key}++;
    $self->log()->warn( '[' . $egene->stable_id() .
                          "] translate() - $status_key: " . $etranscript->start,
                        '-',
                        $etranscript->end,
                        '(' . $etranscript->strand . '):' . $@ );
  }

  # Check for translation failure
  #   Not using a valid initiator codon!
  #   Not using a valid terminator codon!
  #   Terminator codon inside CDS!
  elsif ( $aa_seq =~ /\*/x ) {
    my $status_key = 'fail' . $fuzzy_label;
    $self->{translation_stats}{$status_key}++;
    $self->log()->warn( '[' . $egene->stable_id() .
                          "] translate() - $status_key: " . $etranscript->start,
                        '-',
                        $etranscript->end,
                        '(' . $etranscript->strand . '):translateable_seq' .
                          $aa_seq );
  }
  else {
    my $status_key = 'ok' . $fuzzy_label;
    $self->{translation_stats}{$status_key}++;
  }
  return;
} ## end sub set_transcript_stats
1;
__END__

=head1 NAME

GenomeLoader::FeatureLoader::ProteinCodingGeneLoader

=head1 SYNOPSIS

Create

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
  Description: Create new Bio::EnsEMBL::Gene object based on supplied hash
  Args       : gene hash, slice
  Returns    : Bio::EnsEMBL::Gene

=head2 store_proteins
  Title      : store_proteins
  Description: Create transcripts and translations based on supplied hash for the given gene
  Args       : target gene, gene hash
  Returns    : array of protein features belonging to transcripts

=head2 get_exons
  Title      : get_exons
  Description: Create exons for a transcript
  Args       : Protein hash
  Returns    : CDS length

=head2 check_exons
  Title      : check_exons
  Description: Confirm that exons do not span origin. Remove if they do.
  Args       : slice, list of exons

=head2 get_cds_length
  Title      : determine coding sequence length for a given location
  Description: Confirm that exons do not span origin
  Args       : Protein location hash
  Returns    : CDS length

=head2 get_translation
  Title      : get_translation
  Description: create translation
  Args       : array of exons, translation strand, frame, start, end, parent transcript
  Returns    : Bio::Ensembl::Translation

=head2 get_translation_attribs
  Title      : get_translation_attribs
  Description: get translation attributes which show if 5' and 3' ends are fuzzy
  Args       : location hash
  Returns    : pair of translation attribs

=head2 location_mod_to_attribute
  Title      : location_mod_to_attribute
  Description: turn a location modification into a seq edit
  Args       : location modification
  Returns    : Bio::EnsEMBL::SeqEdit

=head2 set_exons_phases
  Title      : set_exons_phases
  Description: correct the phases and ordering of exons
  Args       : translation, exons

=head2 set_transcript_stats
  Title      : set_transcript_stats
  Description: add statistics for transcript (obsolete?)
  Args       : transcript, gene, fuzziness
