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
package Bio::EnsEMBL::GenomeLoader::GeneLoader::RRNAGeneLoader;
use warnings;
use strict;
use Bio::EnsEMBL::Gene;
use Bio::EnsEMBL::Exon;
use Bio::EnsEMBL::Transcript;
use Bio::EnsEMBL::GenomeLoader::Constants qw(XREFS NAMES BIOTYPES);
use Carp;
use base qw(GenomeLoader::GeneLoader::RnaGeneLoader);

sub new {
	my $caller = shift;
	my $class  = ref($caller) || $caller;
	my $self   = $class->SUPER::new(@_);
	return $self;
}

sub get_rna_display_xref {
	my ( $self, $egene, $igene ) = @_;
	my $display_xref  = $self->find_xref($egene, XREFS()->{EMBL});
	if(!$display_xref) {

	    if ($egene->analysis->logic_name eq "ncRNA-ENA" || lc $egene->analysis->logic_name eq "ena_rna") {
		    $display_xref = $self->get_xref_for_name($egene,XREFS()->{EMBL_GENE_NAME},$igene->{name});
	    }
	    else {
		    $display_xref = $self->get_xref_for_name($egene,XREFS()->{RNAMMER},$igene->{name});
	    }
	}

	# Add synonyms - required for EMBL ncRNA genes
	# make sure it behaves fine for computed ncRNA genes (we don't want any synonym in these cases)

	if (defined $display_xref) {
	    $self->{displayXrefFinder}->add_synonyms($display_xref, $igene);
	}
	
	return $display_xref;
}

1;
__END__

=head1 NAME

GenomeLoader::FeatureLoader::RRNAGeneLoader

=head1 SYNOPSIS

Create a new gene for a rRNA gene

=head1 AUTHORS

Dan Staines <dstaines@ebi.ac.uk>

=head1 METHODS

=head2 new
  Title      : new
  Description: Constructor. Invokes BaseLoader->new as well
  Args       : Hash of arguments
  Returns    : new instance

=head2 get_rna_display_xref
  Title      : get_rna_display_xref
  Description: get xref for RNAMMER to use as display xref
  Args       : Bio::EnsEMBL::Gene, gene hash
  Returns    : Bio::EnsEMBL::DBEntry
