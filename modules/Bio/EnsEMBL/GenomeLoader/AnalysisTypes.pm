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
# Module storing analysis types used by genomeloader
#
package Bio::EnsEMBL::GenomeLoader::AnalysisTypes;
use warnings;
use strict;
use Bio::EnsEMBL::Analysis;
use Carp;
use Exporter 'import';
our @EXPORT_OK =
  qw(get_all_types get_named_type to_analysis_obj get_type add_type);
my $anal = {

	ENA => { -NUM           => 1001,
			 -LOGIC_NAME    => 'ena',
			 -DB            => 'ena',
			 -DESCRIPTION   => 'Protein coding genes annotated in ENA',
			 -DISPLAY_LABEL => 'ENA protein coding genes',
			 -DISPLAYABLE   => '1',
			 -GFF_SOURCE    => undef,
			 -GFF_FEATURE   => undef,
			 -WEB_DATA      => {
							'caption'       => 'ENA Genes',
							'multi_caption' => 'ENA Genes',
							'name'          => 'Protein coding genes annotated in ENA',
							'label_key'     => '[biotype]',
							'colour_key'    => '[biotype]',
							'key'           => 'ena_genes',
							'default'       => {
								  'contigviewbottom'     => 'transcript_label',
								  'contigviewtop'        => 'gene_label',
								  'cytoview'             => 'gene_label',
								  'MultiTop'             => 'gene_label',
								  'MultiBottom'          => 'collapsed_label',
								  'alignsliceviewbottom' => 'as_collapsed_label'
							} } },
	ENA_RNA => { -NUM           => 1101,
				 -LOGIC_NAME    => 'ena_rna',
				 -DB            => 'ena_rna',
				 -DESCRIPTION   => 'Non-coding RNA genes annotated in ENA',
				 -DISPLAY_LABEL => 'ENA ncRNA genes',
				 -DISPLAYABLE   => '1',
				 -GFF_SOURCE    => undef,
				 -GFF_FEATURE   => undef,
				 -WEB_DATA      => {
							  'caption'       => 'ENA Genes',
							  'multi_caption' => 'ENA Genes',
							  'name'          => 'Non-coding RNA genes annotated in ENA',
							  'label_key'     => '[biotype]',
							  'colour_key'    => '[biotype]',
							  'key'           => 'ena_genes',
							  'default'       => {
								  'contigviewbottom'     => 'transcript_label',
								  'contigviewtop'        => 'gene_label',
								  'cytoview'             => 'gene_label',
								  'MultiTop'             => 'gene_label',
								  'MultiBottom'          => 'collapsed_label',
								  'alignsliceviewbottom' => 'as_collapsed_label'
							  } } },
	GENEDB_SPOMBE => {
				-NUM        => 1102,
				-LOGIC_NAME => 'genedb_spombe',
				-DB         => 'GeneDB_Spombe',
				-DESCRIPTION =>
				  'Protein coding genes imported from GeneDB_Spombe annotation',
				-DISPLAY_LABEL => 'GeneDB_Spombe',
				-DISPLAYABLE   => '1',
				-GFF_SOURCE    => undef,
				-GFF_FEATURE   => undef,
				-WEB_DATA      => {
							  'caption'    => 'GeneDB_Spombe',
							  'name'       => 'GeneDB_Spombe Genes',
							  'label_key'  => '[text_label] [display_label]',
							  'colour_key' => '[biotype]',
							  'default'    => {
								  'contigviewbottom'     => 'transcript_label',
								  'contigviewtop'        => 'gene_label',
								  'cytoview'             => 'gene_label',
								  'MultiTop'             => 'gene_label',
								  'MultiBottom'          => 'collapsed_label',
								  'alignsliceviewbottom' => 'as_collapsed_label'
							  } } },
	ENSEMBLBACTERIA_ALIGNMENT => {
						  -NUM           => 1103,
						  -LOGIC_NAME    => 'ensembl_bacteria_alignment',
						  -DB            => 'ensembl_bacteria_alignment',
						  -DESCRIPTION   => 'Ensembl bacteria alignment',
						  -DISPLAY_LABEL => 'Ensembl bacteria alignment',
						  -DISPLAYABLE   => '1',
						  -GFF_SOURCE    => undef,
						  -GFF_FEATURE   => undef,
						  -WEB_DATA      => {
							  'caption'       => 'Ensembl Bacteria Genes',
							  'multi_caption' => 'Ensembl Bacteria Genes',
							  'name'          => 'Ensembl Bacteria alignment',
							  'label_key'     => '[text_label] [display_label]',
							  'colour_key'    => '[biotype]',
							  'default'       => {
								  'contigviewbottom'     => 'transcript_label',
								  'contigviewtop'        => 'gene_label',
								  'cytoview'             => 'gene_label',
								  'MultiTop'             => 'gene_label',
								  'MultiBottom'          => 'collapsed_label',
								  'alignsliceviewbottom' => 'as_collapsed_label'
							  } } },
	ENSEMBLPROTISTS_ALIGNMENT => {
						  -NUM           => 1104,
						  -LOGIC_NAME    => 'ensembl_protists_alignment',
						  -DB            => 'ensembl_protists_alignment',
						  -DESCRIPTION   => 'Ensembl Protists alignment',
						  -DISPLAY_LABEL => 'Ensembl Protists alignment',
						  -DISPLAYABLE   => '1',
						  -GFF_SOURCE    => undef,
						  -GFF_FEATURE   => undef,
						  -WEB_DATA      => {
							  'caption'       => 'Ensembl Genes',
							  'multi_caption' => 'Ensembl Genes',
							  'name'          => 'Ensembl Protists alignment',
							  'label_key'     => '[text_label] [display_label]',
							  'colour_key'    => '[biotype]',
							  'default'       => {
								  'contigviewbottom'     => 'transcript_label',
								  'contigviewtop'        => 'gene_label',
								  'cytoview'             => 'gene_label',
								  'MultiTop'             => 'gene_label',
								  'MultiBottom'          => 'collapsed_label',
								  'alignsliceviewbottom' => 'as_collapsed_label'
							  } } },
	ENSEMBLFUNGI_ALIGNMENT => {
						  -NUM           => 1105,
						  -LOGIC_NAME    => 'ensembl_fungi_alignment',
						  -DB            => 'ensembl_fungi_alignment',
						  -DESCRIPTION   => 'Ensembl Fungi alignment',
						  -DISPLAY_LABEL => 'Ensembl Fungi alignment',
						  -DISPLAYABLE   => '1',
						  -GFF_SOURCE    => undef,
						  -GFF_FEATURE   => undef,
						  -WEB_DATA      => {
							  'caption'       => 'Ensembl Genes',
							  'multi_caption' => 'Ensembl Genes',
							  'name'          => 'Ensembl Fungi alignment',
							  'label_key'     => '[text_label] [display_label]',
							  'colour_key'    => '[biotype]',
							  'default'       => {
								  'contigviewbottom'     => 'transcript_label',
								  'contigviewtop'        => 'gene_label',
								  'cytoview'             => 'gene_label',
								  'MultiTop'             => 'gene_label',
								  'MultiBottom'          => 'collapsed_label',
								  'alignsliceviewbottom' => 'as_collapsed_label'
							  } } },
	ENA_ALIGNMENT => {
						  -NUM           => 1106,
						  -LOGIC_NAME    => 'ena_alignment',
						  -DB            => 'ena_alignment',
						  -DESCRIPTION   => 'ENA Genomes alignment',
						  -DISPLAY_LABEL => 'ENA Genomes alignment',
						  -DISPLAYABLE   => '1',
						  -GFF_SOURCE    => undef,
						  -GFF_FEATURE   => undef,
						  -WEB_DATA      => {
							  'caption'       => 'Ensembl Genes',
							  'multi_caption' => 'Ensembl Genes',
							  'name'          => 'ENA Genomes alignment',
							  'label_key'     => '[text_label] [display_label]',
							  'colour_key'    => '[biotype]',
							  'default'       => {
								  'contigviewbottom'     => 'transcript_label',
								  'contigviewtop'        => 'gene_label',
								  'cytoview'             => 'gene_label',
								  'MultiTop'             => 'gene_label',
								  'MultiBottom'          => 'collapsed_label',
								  'alignsliceviewbottom' => 'as_collapsed_label'
							  } } },
	NCRNA => {
		-NUM        => 1011,
		-LOGIC_NAME => 'ncrna',
		-DB         => 'ncRNA',
		-DESCRIPTION =>
'ncRNA genes are predicted using a combination of methods depending on their type. tRNAs are predicted using <a href="http://selab.janelia.org/tRNAscan-SE/">tRNAScan-SE</a>, rRNAs using <a href="http://www.cbs.dtu.dk/services/RNAmmer/">RNAmmer</a>, and for all other types, using covariance models and sequences from <a href="http://rfam.sanger.ac.uk/">RFAM</a>.',
		-DISPLAY_LABEL => 'ncRNA (predicted)',
		-DISPLAYABLE   => '1',
		-GFF_SOURCE    => 'ensembl',
		-GFF_FEATURE   => 'gene',
		-WEB_DATA      => {
					   'caption'       => 'ncRNA Genes',
					   'multi_caption' => 'ncRNA Genes',
					   'name'          => 'ncRNA genes',
					   'label_key'     => '[text_label] [display_label]',
					   'colour_key'    => '[biotype]',
					   'default'       => {
								  'contigviewbottom'     => 'transcript_label',
								  'contigviewtop'        => 'gene_label',
								  'cytoview'             => 'gene_label',
								  'MultiTop'             => 'gene_label',
								  'MultiBottom'          => 'collapsed_label',
								  'alignsliceviewbottom' => 'as_collapsed_label'
					   }, } },
	'NCRNA-ENA' => {
			  -NUM         => 1012,
			  -LOGIC_NAME  => 'ncrna-ena',
			  -DB          => 'ncRNA-ENA',
			  -DESCRIPTION => 'ncRNA genes annotated in the original ENA entry',
			  -DISPLAY_LABEL => 'ncRNA (ENA)',
			  -DISPLAYABLE   => '1',
			  -GFF_SOURCE    => 'ensembl',
			  -GFF_FEATURE   => 'gene',
			  -WEB_DATA      => {
							 'caption'    => 'ncRNA',
							 'name'       => 'ncRNA genes',
							 'label_key'  => '[text_label] [display_label]',
							 'colour_key' => '[biotype]',
							 'default'    => {
								  'contigviewbottom'     => 'transcript_label',
								  'contigviewtop'        => 'gene_label',
								  'cytoview'             => 'gene_label',
								  'MultiTop'             => 'gene_label',
								  'MultiBottom'          => 'collapsed_label',
								  'alignsliceviewbottom' => 'as_collapsed_label'
							 }, } },
	CHAIN => { -NUM           => 1021,
			   -LOGIC_NAME    => 'chainp',
			   -DB            => 'chain_peptide',
			   -DESCRIPTION   => 'Chain Peptide',
			   -DISPLAY_LABEL => 'ChainP',
			   -DISPLAYABLE   => '1',
			   -GFF_SOURCE    => 'ChainP',
			   -GFF_FEATURE   => 'annotation',
			   -WEB_DATA      => { 'type' => 'feature' } },
	PEPTIDE => { -NUM           => 1022,
				 -LOGIC_NAME    => 'peptide',
				 -DB            => 'peptide',
				 -DESCRIPTION   => 'Peptide',
				 -DISPLAY_LABEL => 'Peptide',
				 -DISPLAYABLE   => '1',
				 -GFF_SOURCE    => 'Peptide',
				 -GFF_FEATURE   => 'annotation' },
	PRO_PEPTIDE => { -NUM           => 1023,
					 -LOGIC_NAME    => 'prop',
					 -DB            => 'pro_peptide',
					 -DESCRIPTION   => 'Pro-peptide',
					 -DISPLAY_LABEL => 'ProP',
					 -DISPLAYABLE   => '1',
					 -GFF_SOURCE    => 'Prop',
					 -GFF_FEATURE   => 'annotation',
					 -WEB_DATA      => { 'type' => 'feature' } },
	SIGNAL_PEPTIDE => {
		-NUM        => 1024,
		-LOGIC_NAME => 'signalp',
		-DB         => 'signal_peptide',
		-DESCRIPTION =>
'Prediction of signal peptide cleavage sites by SignalP (J. D. Bendtsen et al. J. Mol. Biol. 2004 340:783-95)',
		-DISPLAY_LABEL => 'SignalP',
		-DISPLAYABLE   => '1',
		-GFF_SOURCE    => 'SignalP',
		-GFF_FEATURE   => 'annotation',
		-WEB_DATA      => { 'type' => 'feature' } },
	TRANSIT_PEPTIDE => { -NUM           => 1025,
						 -LOGIC_NAME    => 'transitp',
						 -DB            => 'transit_peptide',
						 -DESCRIPTION   => 'TransitP',
						 -DISPLAY_LABEL => 'TransitP',
						 -DISPLAYABLE   => '1',
						 -GFF_SOURCE    => 'Transitp',
						 -GFF_FEATURE   => 'annotation' },
	TRANSMEM => {
		-NUM        => 1031,
		-LOGIC_NAME => 'tmhmm',
		-DB         => 'transmembrane',
		-DESCRIPTION =>
'Prediction of transmembrane helices in peptides by TMHMM (A. Krogh et al., J. Mol. Biol. 2001 305:567-80)',
		-DISPLAY_LABEL => 'tmhmm',
		-DISPLAYABLE   => '1',
		-GFF_SOURCE    => 'Tmhmm',
		-GFF_FEATURE   => 'annotation',
		-WEB_DATA      => { 'type' => 'feature' } },
	GENE3D => { -NUM           => 1032,
				-LOGIC_NAME    => 'gene3d',
				-DB            => 'Gene3d',
				-DESCRIPTION   => 'Gene3d',
				-DISPLAY_LABEL => 'Gene3d',
				-DISPLAYABLE   => '1',
				-GFF_SOURCE    => 'Gene3d',
				-GFF_FEATURE   => 'annotation',
				-WEB_DATA      => { 'type' => 'domain' } },
	NCOILS => {
		-NUM        => 1033,
		-LOGIC_NAME => 'ncoils',
		-DB         => 'coiled_coil',
		-DESCRIPTION =>
'Prediction of coiled-coil regions in peptides by Ncoils (A. N. Lupas et al., Science 1991 252:1162-1164)',
		-DISPLAY_LABEL => 'ncoils',
		-DISPLAYABLE   => '1',
		-GFF_SOURCE    => 'ncoils',
		-GFF_FEATURE   => 'annotation' },
	PRODOM => { -NUM           => 1041,
				-LOGIC_NAME    => 'prodom',
				-DB            => 'Prodom',
				-DESCRIPTION   => 'Prodom',
				-DISPLAY_LABEL => 'Prodom',
				-DISPLAYABLE   => '1',
				-GFF_SOURCE    => 'Prodom',
				-GFF_FEATURE   => 'annotation' },
	PFAM => {
		-NUM        => 1042,
		-LOGIC_NAME => 'pfam',
		-DB         => 'Pfam',
		-DESCRIPTION =>
'Hits to the Pfam database (protein family/domain Hidden Markov Models; A. Bateman et al., Nucleic Acids Res. 2004 32:D138-41). Part of the Interpro consortium (http://www.ebi.ac.uk/interpro/)',
		-DISPLAY_LABEL => 'Pfam',
		-DISPLAYABLE   => '1',
		-GFF_SOURCE    => 'Pfam',
		-GFF_FEATURE   => 'domain',
		-WEB_DATA      => { 'type' => 'domain' } },
	PIRSF => {
		-NUM        => 1043,
		-LOGIC_NAME => 'pirsf',
		-DB         => 'PIRSF',
		-DESCRIPTION =>
'Hits to the PIR SuperFamily database (C.H. Wu et al., Nucleic Acids Res. 2004 32:D112-D114). Part of the Interpro Consortium (http://www.ebi.ac.uk/interpro/).',
		-DISPLAY_LABEL => 'PIRSF',
		-DISPLAYABLE   => '1',
		-GFF_SOURCE    => 'PIRSF',
		-GFF_FEATURE   => 'domain',
		-WEB_DATA      => { 'type' => 'domain' } },
	PRINTS => {
		-NUM        => 1044,
		-LOGIC_NAME => 'prints',
		-DB         => 'PRINTS',
		-DESCRIPTION =>
'Hits to the PRINTS database (protein family/domain models based on groups of motifs ("fingerprints"); T. K. Attwood et al., Nuc. Acids. Res. 2003 31:400-2). Part of the Interpro consortium (http://www.ebi.ac.uk/interpro/)',
		-DISPLAY_LABEL => 'Prints',
		-DISPLAYABLE   => '1',
		-GFF_SOURCE    => 'Prints',
		-GFF_FEATURE   => 'domain',
		-WEB_DATA      => { 'type' => 'feature' } },
	SCANPROSITE => {
		-NUM        => 1045,
		-LOGIC_NAME => 'scanprosite',
		-DB         => 'Prosite_patterns',
		-DESCRIPTION =>
'Hits to the PROSITE patterns database (protein family/domain models based on regular expressions; N. Hulu etal., Nuc. Acids. Res. 2004 32:D134-7). Part of the Interpro consortium (http://www.ebi.ac.uk/interpro/)',
		-DISPLAY_LABEL => 'scanprosite',
		-DISPLAYABLE   => '1',
		-GFF_SOURCE    => 'Prosite_Pattern',
		-GFF_FEATURE   => 'domain',
		-WEB_DATA      => { 'type' => 'domain' } },
	PFSCAN => {
		-NUM        => 1046,
		-LOGIC_NAME => 'pfscan',
		-DB         => 'Prosite_profiles',
		-DESCRIPTION =>
'Hits to the PROSITE profiles database (protein family/domain models based on weight matrices; N. Hulu et al., Nuc. Acids. Res. 2004 32:D134-7). Part of the Interpro consortium (http://www.ebi.ac.uk/interpro/)',
		-DISPLAY_LABEL => 'pfscan',
		-DISPLAYABLE   => '1',
		-GFF_SOURCE    => 'Profile',
		-GFF_FEATURE   => 'domain',
		-WEB_DATA      => { 'type' => 'domain' } },
	SEG => {
		-NUM        => 1061,
		-LOGIC_NAME => 'seg',
		-DB         => 'low_complexity',
		-DESCRIPTION =>
'Identification of peptide low complexity sequences by Seg (J. C. Wooten et al., Comput. Chem. 1993 17:149-163)',
		-DISPLAY_LABEL => 'Seg',
		-DISPLAYABLE   => '1',
		-GFF_SOURCE    => 'Seg',
		-GFF_FEATURE   => 'annotation' },
	PANTHER => { -NUM           => 1047,
				 -LOGIC_NAME    => 'panther',
				 -DB            => 'Panther',
				 -DESCRIPTION   => 'Panther',
				 -DISPLAY_LABEL => 'Panther',
				 -DISPLAYABLE   => '1',
				 -GFF_SOURCE    => 'Panther',
				 -GFF_FEATURE   => 'domain',
				 -WEB_DATA      => { 'type' => 'domain' } },
	SMART => {
		-NUM        => 1048,
		-LOGIC_NAME => 'smart',
		-DB         => 'Smart',
		-DESCRIPTION =>
'Hits to the SMART database (I. Letunic et al., Nucleic Acids Res. 2006 34:D257-D260). Part of the Interpro consortium (http://www.ebi.ac.uk/interpro/)',
		-DISPLAY_LABEL => 'Smart',
		-DISPLAYABLE   => '1',
		-GFF_SOURCE    => 'Smart',
		-GFF_FEATURE   => 'domain',
		-WEB_DATA      => { 'type' => 'domain' } },
	SUPERFAMILY => {
		-NUM        => 1049,
		-LOGIC_NAME => 'superfamily',
		-DB         => 'Superfamily',
		-DESCRIPTION =>
'Hits to the SUPERFAMILY database (J. Gough et al., Journal Molecular Biol. 2001 313:903-919). Part of the Interpro consortium (http://www.ebi.ac.uk/interpro/)',
		-DISPLAY_LABEL => 'Superfamily',
		-DISPLAYABLE   => '1',
		-GFF_SOURCE    => 'Superfamily',
		-GFF_FEATURE   => 'domain',
		-WEB_DATA      => { 'type' => 'domain' } },
	TIGRFAM => {
		-NUM        => 1050,
		-LOGIC_NAME => 'tigrfam',
		-DB         => 'TIGRfam',
		-DESCRIPTION =>
'Hits to the TIGRFAMs database (D.H. Haft et al., Nucleic Acids Res. 2003 31:371-373). Part of the Interpro consortium (http://www.ebi.ac.uk/interpro/)',
		-DISPLAY_LABEL => 'Tigrfam',
		-DISPLAYABLE   => '1',
		-GFF_SOURCE    => 'TIGRFAM',
		-GFF_FEATURE   => 'domain',
		-WEB_DATA      => { 'type' => 'domain' } },
	OTHER_PROTEIN => {
		-NUM        => 1062,
		-LOGIC_NAME => 'other_protein',
		-DB         => 'other_protein',
		-DESCRIPTION =>
'Uniprot proteins aligned to the genome with GeneWise (E.Birney et al., Genome Res. 2004 14:988-95)',
		-DISPLAY_LABEL => 'otherprotein',
		-DISPLAYABLE   => '1',
		-GFF_SOURCE    => undef,
		-GFF_FEATURE   => undef },
	XREFPROTEIN => { -NUM           => 1063,
					 -LOGIC_NAME    => 'xrefprotein',
					 -DB            => undef,
					 -DESCRIPTION   => 'match',
					 -DISPLAY_LABEL => undef,
					 -DISPLAYABLE   => '0',
					 -GFF_SOURCE    => undef,
					 -GFF_FEATURE   => undef },
	DUST => { -NUM         => 1071,
			  -LOGIC_NAME  => 'dust',
			  -DB          => 'Dust',
			  -DESCRIPTION => 'Low-complexity DNA sequence identified by Dust',
			  -DISPLAY_LABEL => 'Dust',
			  -DISPLAYABLE   => '1',
			  -GFF_SOURCE    => undef,
			  -GFF_FEATURE   => undef },
	TRF => {
		-NUM        => 1072,
		-LOGIC_NAME => 'trf',
		-DB         => 'TRF',
		-DESCRIPTION =>
'Tandem repeats identified by Tandem Repeats Finder (G. Benson, Nuc. Acids Res. 1999 27:573-580)',
		-DISPLAY_LABEL => 'TRF',
		-DISPLAYABLE   => '1',
		-GFF_SOURCE    => 'trF',
		-GFF_FEATURE   => 'tandem_repeat' },

	ALIEN_HUNTER => {
		-NUM        => 1073,
		-LOGIC_NAME => 'alien_hunter',
		-DB         => 'Alien_hunter',
		-DESCRIPTION =>
'Putative genomic islands are predicted using <a href="http://www.sanger.ac.uk/Software/analysis/alien_hunter/">alien_hunter</a> software, based on sequence compositional biases.',
		-DISPLAY_LABEL => 'Alien_hunter',
		-DISPLAYABLE   => '1',
		-GFF_SOURCE    => undef,
		-GFF_FEATURE   => undef },

#NB: The parameter line is not complete as there is also a -species parameter which is specific of each species, I don't know how we can generate the specific species name automatically
#e.g. plasmodium vivax => the parameter attribute should be :
#'-no_is -norna -nolow -species plasmodium vivax'
	REPEATMASK => {
		-NUM             => 1074,
		-LOGIC_NAME      => 'repeatmask',
		-DB              => 'repbase',
		-DB_VERSION      => '20090120',
		-DB_FILE         => 'repbase',
		-PROGRAM         => 'RepeatMasker',
		-PROGRAM_VERSION => '3.2.7',
		-PROGRAM_FILE    => 'RepeatMasker',
		-PARAMETERS =>
		  '-no_is -norna -nolow',    #TODO should include -species as well
		-MODULE      => 'RepeatMasker',
		-GFF_SOURCE  => 'RepeatMasker',
		-GFF_FEATURE => 'repeat',
		-DESCRIPTION =>
'<a rel="external" href="http://www.repeatmasker.org">RepeatMasker</a> is used to find repeats and low-complexity sequences.  This track usually shows repeats alone (not low-complexity sequences).',
		-DISPLAY_LABEL => 'Repeats',
		-DISPLAYABLE   => '1', },
	CPG => {
		-NUM             => 1075,
		-LOGIC_NAME      => 'cpg',
		-DB              => 'cpg',
		-PROGRAM         => 'cpg',
		-PROGRAM_VERSION => '1',
		-PROGRAM_FILE    => 'cpg',
		-MODULE          => 'CPG',
		-GFF_SOURCE      => 'cpg',
		-GFF_FEATURE     => 'cpg_island',
		-DESCRIPTION =>
'CpG islands are regions of nucleic acid sequence containing a high number of adjacent cytosine guanine pairs (along one strand).  Usually unmethylated, they are associated with promoters and regulatory regions.  They are determined from the genomic sequence using a program written by G. Miklem, similar to <a rel="external" href="http://emboss.sourceforge.net/apps/cvs/emboss/apps/newcpgreport.html">newcpgreport</a> in the EMBOSS package.',
		-DISPLAY_LABEL => 'CpG islands',
		-DISPLAYABLE   => '1', },
	EPONINE => {
		-NUM          => 1076,
		-LOGIC_NAME   => 'eponine',
		-DB           => 'eponine',
		-PROGRAM      => 'eponine-scan',
		-PROGRAM_FILE => 'java',
		-PARAMETERS =>
'-epojar => /usr/local/ensembl/lib/eponine-scan.jar, -threshold => 0.999',
		-MODULE => 'EponineTSS',
		-DESCRIPTION =>
'Transcription start sites predicted by <a rel="external" href="http://www.sanger.ac.uk/Users/td2/eponine/">Eponine-TSS</a>.',
		-DISPLAY_LABEL => 'TSS (Eponine)',
		-DISPLAYABLE   => '1', },
	HAMAP => {
		-NUM        => 1077,
		-LOGIC_NAME => 'hamap',
		-DB         => 'HAMAP',
		-DESCRIPTION =>
'HAMAP is a system, based on manual protein annotation, that identifies and semi-automatically annotates proteins that are part of well-conserved families or subfamilies: the HAMAP families. HAMAP is based on manually created family rules and is applied to bacterial, archaeal and plastid-encoded proteins',
		-DISPLAY_LABEL => 'HAMAP',
		-DISPLAYABLE   => '1',
		-GFF_SOURCE    => 'HAMAP',
		-GFF_FEATURE   => 'domain',
		-WEB_DATA      => { 'type' => 'domain' } },
	ENA_REPEAT => { -NUM           => 1078,
					-LOGIC_NAME    => 'ena_repeat',
					-GFF_SOURCE    => 'ena_repeat',
					-GFF_FEATURE   => 'ena_repeat',
					-DESCRIPTION   => 'Repeat regions annotated in ENA',
					-DISPLAY_LABEL => 'ENA Repeats',
					-DISPLAYABLE   => '1', },
	GL_XREF => { -NUM           => 1079,
					-LOGIC_NAME    => 'gl_xref',
					-DESCRIPTION   => 'Cross-references attached by GenomeLoader',
					-DISPLAY_LABEL => 'GenomeLoader cross-references',
					-DISPLAYABLE   => '1', },
	GL_NAME_XREF => { -NUM           => 1080,
					-LOGIC_NAME    => 'gl_name_xref',
					-DESCRIPTION   => 'Cross-references attached by GenomeLoader to provide names',
					-DISPLAY_LABEL => 'GenomeLoader name cross-references',
					-DISPLAYABLE   => '1', },
};

sub get_all_types {
	return values %{$anal};
}

sub get_named_type {
	my ($type) = @_;
	return $anal->{ uc $type };
}
my $_anal_cache = ();

sub to_analysis_obj {
	my ($type) = @_;
	croak('Cannot convert null analysis type') if !$type;
	my $anal = $_anal_cache->{$type};
	if ( !$anal ) {
		$anal =
		  Bio::EnsEMBL::Analysis->new(-LOGIC_NAME    => $type->{-LOGIC_NAME},
									  -DB            => $type->{-DB},
									  -DESCRIPTION   => $type->{-DESCRIPTION},
									  -DISPLAY_LABEL => $type->{-DISPLAY_LABEL},
									  -DISPLAYABLE   => $type->{-DISPLAYABLE},
									  -GFF_SOURCE    => $type->{-GFF_SOURCE},
									  -GFF_FEATURE   => $type->{-GFF_FEATURE},
									  -WEB_DATA      => $type->{-WEB_DATA} );
		$_anal_cache->{$type} = $anal;
	}
	return $anal;
}

sub add_type {
	my ( $name, $type ) = @_;
	$anal->{ uc $name } = $type;
	return;
}

sub get_type {
	my $type = get_named_type(@_);
	return to_analysis_obj($type);
}
1;
__END__

=head1 NAME

GenomeLoader::AnalysisTypes

=head1 SYNOPSIS

Helper class supplying access to controlled list of analysis types.

=head1 AUTHORS

Dan Staines <dstaines@ebi.ac.uk>

=head1 METHODS

=head2 get_type
  Title      : get_type
  Description: get analysis object with logic name as specified
  Args       : name
  Returns	 : Bio::EnsEMBL::Analysis

=head2 get_named_type
  Title      : get_named_type
  Description: get analysis hash with logic name as specified. This is used internally to generate analysis objects from a controlled list
  Args       : name
  Returns	 : hash of analysis terms

=head2 to_analysis_obj
  Title      : to_analysis_obj
  Description: turn analysis hash into object. This is used internally to generate analysis objects from a controlled list
  Args       : analysis hash
  Returns	 : Bio::EnsEMBL::Analysis

=head2 get_all_types
  Title      : get_all_types
  Description: Get all the controlled hashes of analyses.
  Args       : none
  Returns	 : list of hashes of analysis terms
