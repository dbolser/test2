
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
package Bio::EnsEMBL::GenomeLoader::GeneLoader::RnaGeneLoader;
use warnings;
use strict;
use Bio::EnsEMBL::Gene;
use Bio::EnsEMBL::Exon;
use Bio::EnsEMBL::Transcript;
use GenomeLoader::Constants qw(XREFS NAMES BIOTYPES);
use Carp;
use Data::Dumper;
use base qw(GenomeLoader::GeneLoader);

sub new {
	my $caller = shift;
	my $class  = ref($caller) || $caller;
	my $self   = $class->SUPER::new(@_);
	return $self;
}

sub load_gene {
	my ( $self, $igene, $slice ) = @_;

	# Create gene.
	my $time    = time();
	my $biotype = $igene->{biotype};

	if ( ( $biotype eq "tRNA" ) && $igene->{pseudogene} ) {
		$biotype = "tRNA_pseudogene";
	}

	my $src = $self->config()->{source};
	if ( $igene->{analysis} eq 'RFAM_GENES' ) {
		$src = 'Rfam';
	}

	my $egene = Bio::EnsEMBL::Gene->new(
		-SLICE    => $slice,
		-BIOTYPE  => $biotype,
		-SOURCE   => $src,
		-ANALYSIS => $self->analysis_finder()
		  ->get_analysis_by_name( $igene->{analysis}, "gene" ),
		-CREATED_DATE  => $time,
		-MODIFIED_DATE => $time
	);

	$egene->description( $igene->{description} ) if ( $igene->{description} );
	$self->set_stable_id( $igene, $egene );

	# TODO add source here too?
	my @igene_xrefs;
	my @itranscript_xrefs;
	foreach my $xref ( @{ $igene->{xrefs} } ) {
		my $ensembl_dbname = $xref->{databaseReferenceType}{ensemblName};

		# RFAM xrefs on gene are moved to transcript.
		if ( $ensembl_dbname eq XREFS()->{RFAM} ) {
			push( @itranscript_xrefs, $xref );
		}
		else {
			push( @igene_xrefs, $xref );
		}
		if ( $ensembl_dbname eq "ENA_FEATURE_GENE" ) {
			my $txref = {
				"databaseReferenceType" =>
				  { "ensemblName" => "ENA_FEATURE_TRANSCRIPT" },
				"primaryIdentifier" => $xref->{primaryIdentifier}
			};
			push( @itranscript_xrefs, $txref );
		}
	}
	$igene->{xrefs} = \@igene_xrefs;

	# Set gene xrefs (including GO xrefs).
	$self->set_xrefs( $igene, $egene );

	# Create transcripts.
	my $transcriptN = 0;
	foreach my $itranscript ( @{ $igene->{transcripts} } ) {
		foreach my $itranscript_location ( @{ $itranscript->{locations} } ) {

			# Create exons
			my @eexons;
			my $seqedits = [];
			my $exonN    = 0;

			my $inserts = $itranscript_location->{insertions};

			my $ilocations;
			if ( defined $itranscript_location->{sublocations}
				&& @{ $itranscript_location->{sublocations} } > 0 )
			{
				$ilocations = $itranscript_location->{sublocations};

# Sort by Integr8 (EMBL) rank.
# This means that exons are ordered by location ascending value, regardless of strand.
# Note: ensembl rank is the order in which exons are used in the translation.
# (ie. ensembl rank is opposite to Integr8 rank for reverse strand).
				my @sorted =
				  sort { $a->{rank} <=> $b->{rank} } @{$ilocations};
				$ilocations = \@sorted;
			}
			else {
				$ilocations = [$itranscript_location];
			}
			my $offset = 0;
			foreach my $itranscript_location ( @{$ilocations} ) {

				# Check for : Start must be less than or equal to end+1
				if ( $itranscript_location->{min} >
					$itranscript_location->{max} + 1 )
				{
					$self->log()->warn(
						"Start must be less than or equal to end+1: RNA gene ",
						$egene->stable_id . '/'
						  . $itranscript->{analysis} . '/'
						  . $egene->biotype
						  . ' exon='
						  . $itranscript_location->{min} . '-'
						  . $itranscript_location->{max}
					);
					my $min = $itranscript_location->{min};
					$itranscript_location->{min} = $itranscript_location->{max};
					$itranscript_location->{max} = $min;
				}

				my $time  = time();
				my $eexon = Bio::EnsEMBL::Exon->new(
					-SLICE   => $slice,
					-START   => $itranscript_location->{min},
					-END     => $itranscript_location->{max},
					-STRAND  => $itranscript_location->{strand},
					-PHASE         => 0,       #forced phase to 0
					-END_PHASE     => 0,
					-CREATED_DATE  => $time,
					-MODIFIED_DATE => $time
				);
				$self->set_stable_id( {}, $eexon, $egene, ++$exonN );
				push( @eexons, $eexon );

				# try and find an insert
				if ( defined $inserts ) {
					for my $i ( @{$inserts} ) {
						if (   $i->{start} >= $itranscript_location->{min}
							&& $i->{stop} <= $itranscript_location->{max} )
						{
							my ( $start, $end );
							if ( $itranscript_location->{strand} == 1 ) {
								$start =
								  $i->{start} -
								  $itranscript_location->{min} + 1 +
								  $offset;
								$end =
								  $i->{stop} -
								  $itranscript_location->{min} + 1 +
								  $offset;

							}
							else {

								$start =
								  $i->{start} -
								  $itranscript_location->{max} + 1 +
								  $offset;
								$end =
								  $i->{stop} -
								  $itranscript_location->{max} + 1 +
								  $offset;

							}

							my $seq_edit = Bio::EnsEMBL::SeqEdit->new(
								-CODE        => NAMES()->{RNA_SEQ_EDIT},
								-NAME        => NAMES()->{RNA_SEQ_EDIT},
								-DESCRIPTION => NAMES()->{RNA_SEQ_EDIT},
								-ALT_SEQ     => $i->{seq},
								-START       => $start,
								-END         => $end
							);
							push @{$seqedits}, $seq_edit->get_Attribute;
						}
					} ## end for my $i (@{$inserts})
				} ## end if (defined $inserts)

				$offset +=
				  $itranscript_location->{max} -
				  $itranscript_location->{min} + 1;

			} ## end foreach my $itranscript_location ...
			my $time        = time();
			my $etranscript = Bio::EnsEMBL::Transcript->new(
				-BIOTYPE       => $egene->biotype(),
				-SLICE         => $slice,
				-ANALYSIS      => $egene->analysis(),
				-CREATED_DATE  => $time,
				-SOURCE        => $src,
				-MODIFIED_DATE => $time
			);

			for my $seqedit ( @{$seqedits} ) {
				$etranscript->add_Attributes($seqedit);
			}
			$self->set_stable_id( { identifyingId => $itranscript->{identifyingId} },
				$etranscript, $egene, ++$transcriptN );

			# Add exons to transcripts.
			my $i = 0;
			foreach my $eexon (@eexons) {
				$etranscript->add_Exon($eexon);
			}

			# Put Rfam xrefs extracted from the gene onto the transcript.
			$itranscript->{xrefs} = \@itranscript_xrefs;

			# Set transcript xrefs (including GO xrefs).
			$self->set_xrefs( $itranscript, $etranscript );

 # The display Xref for transcript should be aidentical to the gene display Xref
 # for all ncRNAs should be:   1796 | snoRNA11        | RF00614       |
 # Add transcript to gene.
			$egene->add_Transcript($etranscript);
		} ## end foreach my $itranscript_location ...
	}

	$self->set_rna_display_xref( $egene, $igene );

	# Store the gene, but only if it has at least one transcript.
	if ( @{ $egene->get_all_Transcripts() } > 0 ) {
		eval { $self->dba()->get_GeneAdaptor->store($egene); };
		if ($@) {
			croak( $self->store_gene_error_handler( $igene, $egene ) );
		}
	}
	return;
} ## end sub load_gene

sub find_xref {
	my ( $self, $egene, $db_name ) = @_;
	my $display_xref;
	for my $xref ( @{ $egene->get_all_DBEntries() } ) {
		if ( $db_name eq $xref->dbname() ) {
			$display_xref = $xref;
			last;
		}
	}
	return $display_xref;
}

sub get_xref_for_transcript {
	my ( $self, $egene, $db_name ) = @_;
	my $display_xref;
	for my $etranscript ( @{ $egene->get_all_Transcripts() } ) {
		for my $xref ( @{ $etranscript->get_all_DBEntries() } ) {
			if ( $db_name eq $xref->dbname() ) {
				$display_xref = $xref;
				last;
			}
		}
		if ($display_xref) {
			last;
		}
	}
	return $display_xref;
}

sub get_xref_for_name {
	my ( $self, $egene, $db_name, $gene_name ) = @_;
	if ( !$gene_name ) {
		croak "Cannot create a $db_name DBEntry for an empty gene name";
	}
	my $display_xref = Bio::EnsEMBL::DBEntry->new(
		-DBNAME     => $db_name,
		-PRIMARY_ID => $gene_name,
		-DISPLAY_ID => $gene_name,
		-ANALYSIS   => $self->{gl_name_xref}
	);
	$egene->add_DBEntry($display_xref);
	return $display_xref;
}

sub get_rna_display_xref {
	my ( $self, $egene, $igene ) = @_;

	# determine target dbname based on biotype
	my $display_xref;
	if ( $igene->{analysis} eq 'RFAM_GENES' ) {
		$display_xref =
		  $self->get_xref_for_name( $egene, 'RFAM_GENE', $igene->{name} );
	}
	elsif ( $egene->analysis->logic_name eq "ncRNA-EMBL"
		|| lc $egene->analysis->logic_name eq "ena_rna" )
	{
		$display_xref =
		  $self->get_xref_for_name( $egene, XREFS()->{EMBL_GENE_NAME},
			$igene->{name} );
	}
	else {

		# check EMBL then RFAM
		for my $db_name ( XREFS()->{EMBL}, XREFS()->{RFAM} ) {
			$display_xref = $self->find_xref( $egene, $db_name );
			if ($display_xref) {
				last;
			}
		}

		# otherwise create something
		# locate xref with matching name
		if ( !$display_xref ) {
			$display_xref =
			  $self->get_xref_for_transcript( $egene, XREFS()->{RFAM} );
		}

		# otherwise, do like for tRNA and rRNAs, build one,
		# using the name coming from the model_materializer!

	}

	if ( !$display_xref ) {
		$self->log()
		  ->warn( "No display_xref found for RNA gene of type "
			  . $egene->biotype() . "/"
			  . $egene->analysis()->logic_name() );
	}

# Add synonyms - required for EMBL ncRNA genes
# make sure it behaves fine for computed ncRNA genes (we don't want any synonym in these cases)

	if ( defined $display_xref ) {
		$self->{displayXrefFinder}->add_synonyms( $display_xref, $igene );
	}

	return $display_xref;
} ## end sub get_rna_display_xref

sub set_rna_display_xref {
	my ( $self, $egene, $igene ) = @_;
	my $display_xref = $self->get_rna_display_xref( $egene, $igene );
	if ($display_xref) {
		$egene->display_xref($display_xref);
		for my $etranscript ( @{ $egene->get_all_Transcripts() } ) {
			$etranscript->display_xref($display_xref);
		}
	}
	return;
}
1;
__END__

=head1 NAME

GenomeLoader::FeatureLoader::RnaGeneLoader

=head1 SYNOPSIS

Create a new gene for an ncRNA gene

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

=head2 get_rna_display_xref
  Title      : get_rna_display_xref
  Description: get xref for rfam to use as display xref
  Args       : Bio::EnsEMBL::Gene, gene hash
  Returns    : Bio::EnsEMBL::DBEntry

=head2 set_rna_display_xref
  Title      : set_rna_display_xref
  Description: determine display xref and set
  Args       : Bio::EnsEMBL::Gene, gene hash

