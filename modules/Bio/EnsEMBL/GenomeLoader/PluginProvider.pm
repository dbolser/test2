
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

package Bio::EnsEMBL::GenomeLoader::PluginProvider;
use warnings;
use strict;
use Carp;
use Bio::EnsEMBL::GenomeLoader::Constants qw(BIOTYPES NAMES);
use Bio::EnsEMBL::GenomeLoader::Utils qw(from_json_file_default);
use Log::Log4perl qw(get_logger);
use Bio::EnsEMBL::Utils::ScriptUtils qw(inject);

sub new {
  my ($class) = shift;
  $class = ref($class) || $class;
  my $self =
    ( @_ && defined $_[0] && ( ref( $_[0] ) eq 'HASH' ) ) ? $_[0] :
                                                            {@_};
  bless( $self, $class );
  if ( !$self->log() ) {
    $self->log( get_logger() );
  }
  # populate the base set of loaders
  $self->{plugin_names} = {
    sequence      => "GenomeLoader::SequenceLoader",
    simplefeature => "GenomeLoader::FeatureLoader::SimpleFeatureLoader",
    repeatfeature => "GenomeLoader::FeatureLoader::RepeatFeatureLoader",
    BIOTYPES()->{PROTEIN_CODING_GENE_TYPE} =>
      "GenomeLoader::GeneLoader::ProteinCodingGeneLoader",
    BIOTYPES()->{PSEUDOGENE_TYPE} =>
      "GenomeLoader::GeneLoader::PseudoGeneLoader",
    BIOTYPES()->{NON_TRANSLATING_TYPE} =>
      "GenomeLoader::GeneLoader::PseudoGeneLoader",
    BIOTYPES()->{NCRNA_TYPE} =>
      "GenomeLoader::GeneLoader::RnaGeneLoader",
    BIOTYPES()->{MISC_RNA_TYPE} =>
      "GenomeLoader::GeneLoader::RnaGeneLoader",
    BIOTYPES()->{ANTISENSE_TYPE} =>
      "GenomeLoader::GeneLoader::RnaGeneLoader",
    BIOTYPES()->{RNASEP_TYPE} =>
      "GenomeLoader::GeneLoader::RnaGeneLoader",
    BIOTYPES()->{RNASEMRP_TYPE} =>
      "GenomeLoader::GeneLoader::RnaGeneLoader",
    BIOTYPES()->{TMRNA_TYPE} =>
      "GenomeLoader::GeneLoader::RnaGeneLoader",
    BIOTYPES()->{SRP_RNA_TYPE} =>
      "GenomeLoader::GeneLoader::RnaGeneLoader",
    BIOTYPES()->{SN_RNA_TYPE} =>
      "GenomeLoader::GeneLoader::RnaGeneLoader",
    BIOTYPES()->{S_RNA_TYPE} =>
      "GenomeLoader::GeneLoader::RnaGeneLoader",
    BIOTYPES()->{SNO_RNA_TYPE} =>
      "GenomeLoader::GeneLoader::RnaGeneLoader",
    BIOTYPES()->{RRNA_TYPE} =>
      "GenomeLoader::GeneLoader::RRNAGeneLoader",
    BIOTYPES()->{MIRNA_TYPE} =>
      "GenomeLoader::GeneLoader::RnaGeneLoader",
    BIOTYPES()->{ANTITOXIN_TYPE} =>
      "GenomeLoader::GeneLoader::RnaGeneLoader",
    BIOTYPES()->{CRISPR_TYPE} =>
      "GenomeLoader::GeneLoader::RnaGeneLoader",
    BIOTYPES()->{TRNA_TYPE} =>
      "GenomeLoader::GeneLoader::TRNAGeneLoader",
    BIOTYPES()->{RIBOZYME_TYPE} =>
      "GenomeLoader::GeneLoader::TRNAGeneLoader",
    BIOTYPES()->{TRNA_PSEUDO_TYPE} =>
      "GenomeLoader::GeneLoader::TRNAGeneLoader",
    "displayXrefFinder" => "GenomeLoader::DisplayXrefFinder",
    "analysisFinder"    => "GenomeLoader::AnalysisFinder",
    "stableIdFinder"    => "GenomeLoader::StableIdFinder" };

  if ( defined $self->{plugins_file} ) {
    my $user_plugins = from_json_file_default( $self->{plugins_file} );
    for my $key ( keys %$user_plugins ) {
      print "$key => $user_plugins->{$key}\n";
      $self->{plugin_names}{$key} = $user_plugins->{$key};
    }
  }

  # keep a hash
  $self->{plugins} = {};

  return $self;
} ## end sub new

sub config {
  my $self = shift;
  $self->{config} = shift if @_;
  return $self->{config};
}

sub dba {
  my $self = shift;
  $self->{dba} = shift if @_;
  return $self->{dba};
}

sub genome_metadata {
  my $self = shift;
  $self->{genome_metadata} = shift if @_;
  return $self->{genome_metadata};
}

sub log {
  my $self = shift;
  $self->{log} = shift if @_;
  return $self->{log};
}

sub get {
  my $self        = shift;
  my $name        = shift;
  my $plugin_name = $self->{plugin_names}->{$name};
  if ( !defined $plugin_name ) {
    croak "Cannot find plugin for $name\n";
  }
  my $plugin = $self->{plugins}->{$plugin_name};
  if ( !defined $plugin ) {
    inject($plugin_name);
    $plugin = $plugin_name->new(
                       dba      => $self->dba(),
                       division => $self->genome_metadata()->{division},
                       log      => $self->log(),
                       genome_metadata => $self->genome_metadata(),
                       plugins         => $self,
                       config          => $self->config() );
    $self->{plugins}->{$plugin_name} = $plugin;
  }
  return $plugin;
}

1;
