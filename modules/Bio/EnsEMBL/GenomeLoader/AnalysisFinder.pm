
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
# Base object for returning a display_xref for a given object based on rules
# uses dynamic loading to load division specific implementations e.g. EnsemblProtists
#
package Bio::EnsEMBL::GenomeLoader::AnalysisFinder;
use warnings;
use strict;
use Carp;
use Data::Dumper;
use Bio::EnsEMBL::Analysis;

 use Exporter 'import';
 our @EXPORT_OK = qw(get_analysis_by_name);

my $type_to_logicname = { "protein_coding"  => "ena",
                          "eg_alignment"    => "eg_alignment",
                          "repeat"          => "ena_repeat",
                          "ncrna"           => "ncrna-ena",
                          "ena_rna"         => "ena_rna",
                          "pro_peptide"     => "prop",
                          "transmem"        => "tmhmm",
                          "transit_peptide" => "transitp",
                          "other_protein"   => "other_protein",
                          "chain"           => "chainp",
                          "signal_peptide"  => "signalp",
                          "superfamily"     => "superfamily",
                          "ssf"             => "superfamily",
                          "panther"         => "hmmpanther",
                          "prodom"          => "blastprodom",
                          "scanprosite"     => "scanprosite",
                          "prosite"         => "scanprosite",
                          "tigrfams"        => "tigrfam",
                          "10_signal"       => "ena_10_signal",
                          "35_signal"       => "ena_35_signal",
                          "3utr"            => "ena_3utr",
                          "5utr"            => "ena_5utr",
                          "assembly_gap"    => "ena_assembly_gap",
                          "attenuator"      => "ena_attenuator",
                          "direct"          => "ena_repeat_direct",
                          "dispersed"       => "ena_repeat_dispersed",
                          "exon"            => "ena_exon",
                          "flanking"        => "ena_repeat_flanking",
                          "gap"             => "ena_gap",
                          "gene"            => "ena_gene",
                          "intron"          => "ena_intron",
                          "inverted"        => "ena_inverted",
                          "ltr"             => "ena_ltr",
                          "misc_binding"    => "ena_misc_binding",
                          "misc_difference" => "ena_misc_difference",
                          "misc_feature"    => "ena_misc_feature",
                          "misc_recomb"     => "ena_misc_recomb",
                          "misc_rna"        => "ena_misc_rna",
                          "misc_signal"     => "ena_misc_signal",
                          "misc_structure"  => "ena_misc_structure",
                          "mobile_element"  => "ena_mobile_element",
                          "mrna"            => "ena_mrna",
                          "old_sequence"    => "ena_old_sequence",
                          "operon"          => "ena_operon",
                          "orit"            => "ena_orit",
                          "other"           => "ena_repeat",
                          "primer_bind"     => "ena_primer_bind",
                          "promoter"        => "ena_promoter",
                          "protein_bind"    => "ena_protein_bind",
                          "rbs"             => "ena_rbs",
                          "rep_origin"      => "ena_rep_origin",
                          "rfam"            => "rfam_genes",
                          "rfam_genes"      => "rfam_genes",
                          "rrna"            => "ena_rrna",
                          "sig_peptide"     => "ena_sig_peptide",
                          "stem_loop"       => "ena_stem_loop",
                          "tandem"          => "ena_repeat_tandem",
                          "terminator"      => "ena_terminator",
                          "tmrna"           => "ena_tmrna",
                          "trna"            => "ena_trna",
                          "unsure"          => "ena_unsure",
                          "variation"       => "ena_variation" };

my $db_names = { 'prints'       => 'PRINTS',
                 'gene3d'       => 'Gene3D',
                 'hamap'        => 'HAMAP',
                 'hmmpanther'   => 'PANTHER',
                 'pfam'         => 'Pfam',
                 'pirsf'        => 'PIRSF',
                 'blastprodom ' => 'ProDom',
                 'scanprosite'  => 'Prosite_patterns',
                 'superfamily'  => 'Superfamily',
                 'tigrfam'      => 'TIGRfam',
                 'ncoils'       => 'ncoils',
                 'pfscan'       => 'Prosite_profiles',
                 'tmhmm'        => 'Tmhmm',
                 'smart'        => 'Smart',
                 'signalp'      => 'SignalP',
                 'chainp'       => 'chainp',
                 'cdd'          => 'CDD',
                 'seg'          => 'Seq' };

my $anal_hash = {};

sub get_analysis_by_name {
  my ( $name, $type ) = @_;
  $name = lc $name;
  my $logic_name = $type_to_logicname->{$name} || $name;
  my $anal = $anal_hash->{$logic_name};
  if ( !defined $anal ) {
    my $db_name = $db_names->{$logic_name};
    if ( !$db_name ) {
      $db_name = $logic_name;
    }
    if ( $db_name =~ m/ena_.*/ ) {
      $db_name = 'ena';
    }
    $anal = Bio::EnsEMBL::Analysis->new( -LOGIC_NAME  => $logic_name,
                                         -DB          => $db_name,
                                         -GFF_SOURCE  => $logic_name,
                                         -GFF_FEATURE => $type );

    $anal_hash->{$logic_name} = $anal;
  }
  return $anal;
}

1;
