
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
package Bio::EnsEMBL::GenomeLoader::Constants;
use warnings;
use strict;
use Readonly;
use Exporter 'import';
our @EXPORT_OK = qw(CS NAMES BIOTYPES XREFS GENE_NAMES PEPSTATS_CODES);
Readonly my $_CS => { CHROMOSOME    => 'chromosome',
                      PLASMID       => 'plasmid',
                      MITOCHONDRION => 'mitochondrion',
                      CHLOROPLAST   => 'chloroplast',
                      SEGMENT       => 'segment',
                      SCAFFOLD      => 'scaffold',
                      CONTIG        => 'contig',
                      SUPERCONTIG   => 'supercontig' };

sub CS {
  return $_CS;
}

Readonly my $_PEPSTAT_CODES => {'Number of residues' => 'NumResidues',
                                'Molecular weight' => 'MolecularWeight',
                                'Ave. residue weight' => 'AvgResWeight',
                                'Charge'              => 'Charge',
                                'Isoelectric point'   => 'IsoPoint' };

sub PEPSTATS_CODES {
  return $_PEPSTAT_CODES;
}
Readonly my $_NAMES => { SEQUENCE_LEVEL         => 'seqlevel',
                         MAX_SEQUENCE_LEN       => 2e6,
                         DB_TYPE                => 'ena',
                         GENE_ATTRIB_NAME       => 'name',
                         TRANSCRIPT_ATTRIB_NAME => 'name',
                         GENE                   => 'Bio::EnsEMBL::Gene',
                         EXON                   => 'Bio::EnsEMBL::Exon',
                         TRANSCRIPT     => 'Bio::EnsEMBL::Transcript',
                         TRANSLATION    => 'Bio::EnsEMBL::Translation',
                         PERSISTABLE_ID => 'persistableId',
                         IDENTIFYING_ID => 'identifyingId',
                         AMINO_ACID_SUB => 'amino_acid_sub',
                         RNA_SEQ_EDIT   => '_rna_edit' };

sub NAMES {
  return $_NAMES;
}
Readonly my $_GENE_NAMES => { NAME              => 'NAME',
                              ORDEREDLOCUSNAMES => 'ORDEREDLOCUSNAMES',
                              ORFNAMES          => 'ORFNAMES',
                              OTHER             => 'OTHER',
                              SYNONYMS          => 'SYNONYMS' };

sub GENE_NAMES {
  return $_GENE_NAMES;
}
Readonly my $_XREFS => {
                       EMBL                   => 'ENA',
                       EMBL_PROTEIN_QUALIFIER => 'PROTEIN',
                       GO                     => 'GO',
                       IGI                    => 'igi',
                       SGD                    => 'SGD',
                       TAIR                   => 'TAIR',
                       INTACT                 => 'IntAct',
                       INTERPRO               => 'InterPro',
                       TAIR_PROTEIN_QUALIFIER => 'PROTEIN',
                       TAXON                  => 'taxon',
                       UNIPROT_SWISSPROT      => 'Uniprot/SWISSPROT',
                       UNIPROT_TREMBL         => 'Uniprot/SPTREMBL',
                       GENOMIC_DNA            => 'Genomic_DNA',
                       PROTEIN_ID             => 'protein_id',
                       EMBL_DNA               => 'ENA_DNA',
                       RFAM                   => 'RFAM',
                       EMBL_GENE_NAME         => 'ENA_GENE',
                       EB_GENE_NAME           => 'EBACTERIA_GENE',
                       EF_GENE_NAME           => 'EFUNGI_GENE',
                       EPR_GENE_NAME          => 'EPROTISTS_GENE',
                       EMBL_TRANSCRIPT_NAME   => 'ENA_TRANSCRIPT',
                       EB_TRANSCRIPT_NAME     => 'EBACTERIA_TRANSCRIPT',
                       EF_TRANSCRIPT_NAME     => 'EFUNGI_TRANSCRIPT',
                       EPR_TRANSCRIPT_NAME    => 'EPROTISTS_TRANSCRIPT',
                       GENE_STABLE_ID         => 'gene_stable_id',
                       TRANSCRIPT_STABLE_ID   => 'transcript_stable_id',
                       TRANSLATION_STABLE_ID => 'translation_stable_id',
                       RNAMMER               => 'RNAMMER',
                       TRNASCAN              => 'TRNASCAN_SE',
                       RFAM                  => 'RFAM',
                       POMBE                 => 'GeneDB_Spombe',
                       POMBE_TRANSCRIPT => 'GeneDB_Spombe_transcript' };

sub XREFS {
  return $_XREFS;
}

# Gene type for 'protein coding' genes.
Readonly my $_BIOTYPES => {PROTEIN_CODING_GENE_TYPE => 'protein_coding',
                           PSEUDOGENE_TYPE          => 'pseudogene',
                           NON_TRANSLATING_TYPE => 'nontranslating_cds',
                           NCRNA_TYPE           => 'ncRNA',
                           MISC_RNA_TYPE        => 'misc_RNA',
                           ANTISENSE_TYPE       => 'antisense',
                           RNASEP_TYPE          => 'RNase_P_RNA',
                           RNASEMRP_TYPE        => 'RNase_MRP_RNA',
                           TMRNA_TYPE           => 'tmRNA',
                           SRP_RNA_TYPE         => 'SRP_RNA',
                           SNO_RNA_TYPE         => 'snoRNA',
                           SN_RNA_TYPE          => 'snRNA',
                           S_RNA_TYPE           => 'sRNA',
                           RRNA_TYPE            => 'rRNA',
                           TRNA_TYPE            => 'tRNA',
                           MIRNA_TYPE           => 'miRNA',
                           RIBOZYME_TYPE        => 'ribozyme',
                           TRNA_PSEUDO_TYPE     => 'tRNA_pseudogene',
                           ANTITOXIN_TYPE       => 'antitoxin',
                           CRISPR_TYPE          => 'CRISPR' };

sub BIOTYPES {
  return $_BIOTYPES;
}
1;
__END__

=head1 NAME

GenomeLoader::Constants - collection of constants for loading genomes

=head1 SYNOPSIS

A package containing miscellaneous utilities and constants.

=head1 AUTHORS

Dan Staines <dstaines@ebi.ac.uk>
Alan Horne <hornead@ebi.ac.uk>

=head1 CONSTANTS

Constants and names for use by loader scripts

=head2 CS

Coordinate system names

=head2 NAMES

Miscellaneous names

=head2 BIOTYPES

Names of Biotypes

=head2 XREFS

Mappings between names of XREFs from integr8 to ensembl

=head2 GENE_NAMES

Names of genes from integr8 schema
