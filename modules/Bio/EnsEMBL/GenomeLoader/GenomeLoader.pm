
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
package Bio::EnsEMBL::GenomeLoader::GenomeLoader;
use warnings;
use strict;
use Carp;
use Bio::EnsEMBL::CoordSystem;
use Data::Dumper;
use Bio::EnsEMBL::GenomeLoader::Constants qw(BIOTYPES CS);
use Bio::EnsEMBL::GenomeLoader::Utils qw(start_session flush_session);
use Digest::MD5;
use List::MoreUtils qw(uniq);
use base qw(GenomeLoader::BaseLoader);

sub new {
  my $caller = shift;
  my $class  = ref($caller) || $caller;
  my $self   = $class->SUPER::new(@_);
  return $self;
}

sub load_genome_data {
  my ( $self, $genome_metadata ) = @_;

  my $meta = $self->dba()->get_MetaContainer();

  if ( $self->config()->{addComponents} == 1 ) {
    $self->log()->info("Loading components only - skipping genome metadata");
    # reuse existing assembly version
    $genome_metadata->{assemblyDefault} =
      $meta->single_value_by_key('assembly.default');

  }
  else {

    # Store meta data.
    $self->dba()->get_GeneAdaptor();

    # Assembly.
    $self->log()->debug("Setting meta assembly.default");
    $meta->store_key_value( 'assembly.default',
                            $genome_metadata->{assemblyDefault} );
    $self->log()->debug("Setting meta assembly.accession");
    $meta->store_key_value( 'assembly.accession',
                   $genome_metadata->{id} . '.' . $genome_metadata->{version} );
    $self->log()->debug("Setting meta assembly.name");
    $meta->store_key_value( 'assembly.name', $genome_metadata->{assemblyName} );
    $self->log()->debug("Setting meta assembly.date");
    $meta->store_key_value( 'assembly.date', $genome_metadata->{assemblyDate} );

    if ( defined $genome_metadata->{description} ) {
      $meta->store_key_value( 'assembly.description',
                            substr( $genome_metadata->{description}, 0, 255 ) );
    }

    # Gene build.
    $self->log()->debug("Setting meta genebuild.version");
    $meta->store_key_value( 'genebuild.version',
                            $genome_metadata->{buildVersion} );
    $self->log()->debug("Setting meta genebuild.start_date");
    $meta->store_key_value( 'genebuild.start_date',
                            $genome_metadata->{startDate} );
    $self->log()->debug("Setting meta genebuild.initial_release_date");
    $meta->store_key_value( 'genebuild.initial_release_date',
                            $genome_metadata->{startDate} );
    $self->log()->debug("Setting meta genebuild.last_geneset_update");
    $meta->store_key_value( 'genebuild.last_geneset_update',
                            $genome_metadata->{buildDate} );
    $meta->store_key_value( 'genebuild.method',
                            "Generated from ENA annotation" );

    if ( defined $genome_metadata->{masterAccession} ) {
      $meta->store_key_value( 'assembly.master_accession',
                              $genome_metadata->{masterAccession} );
    }

    # Store classification.
    $genome_metadata->{lineage}->[0] = 'unknown'
      unless @{ $genome_metadata->{lineage} };
    $self->log()->debug("Setting meta species.classification");
    foreach my $class ( uniq( @{ $genome_metadata->{lineage} } ) ) {
      $meta->store_key_value( 'species.classification', $class );
    }

    $meta->store_key_value( 'species.production_name',
                            $genome_metadata->{productionName} );
    $meta->store_key_value( 'species.url',
                            ucfirst( $genome_metadata->{productionName} ) );
    $meta->store_key_value( 'species.scientific_name',
                            $genome_metadata->{proteomeName} );
    $meta->store_key_value( 'species.display_name',
                            $genome_metadata->{proteomeName} );
    if ( defined $genome_metadata->{strain} ) {
      $meta->store_key_value( 'species.strain', $genome_metadata->{strain} );
    }
    if ( defined $genome_metadata->{substrain} ) {
      $meta->store_key_value( 'species.substrain',
                              $genome_metadata->{substrain} );
    }
    if ( defined $genome_metadata->{serotype} ) {
      $meta->store_key_value( 'species.serotype',
                              $genome_metadata->{serotype} );
    }

    if ( $genome_metadata->{cladePrefix} ) {
      $self->log()->debug("Setting meta species.stable_id_prefix");
      $meta->store_key_value( 'species.stable_id_prefix',
                              $genome_metadata->{cladePrefix} );
    }

    $self->log()->debug("Setting meta species.alias");

    for my $key (qw/productionName proteomeName fullName/) {
      push @{ $genome_metadata->{aliases} }, $genome_metadata->{$key};
    }

    my $aliases = {};
    for my $alias ( @{ $genome_metadata->{aliases} } ) {
      if ( !defined $aliases->{ lc $alias } ) {
        $meta->store_key_value( 'species.alias', $alias );
        $aliases->{ lc $alias } = 1;
      }
    }

    # Ensembl species db_name.
    $self->log()->debug("Setting meta species.db_name");
    $meta->store_key_value( 'species.db_name',
                            $genome_metadata->{productionName} );

    # Taxonomy. eg. AB002632.species.taxonomy_id	83201
    $self->log()->debug("Setting meta species.taxonomy_id");
    $meta->store_key_value( 'species.taxonomy_id', $genome_metadata->{taxId} );

    # EG Division
    $self->log()->debug("Setting meta species.proteome_id");
    $meta->store_key_value( 'species.division', $genome_metadata->{division} );
    $meta->store_key_value( 'provider.name',    $genome_metadata->{provider} );
    if ( $genome_metadata->{providerUrl} ) {
      $meta->store_key_value( 'provider.url', $genome_metadata->{providerUrl} );
    }
  } ## end else [ if ( $self->config()->...)]

  # Create plasmid coord system.
  my $contig_cs = $self->get_coord_system( CS()->{CONTIG}, 1, 4, 1 );
  my $supercontig_cs = $self->get_coord_system( CS()->{SUPERCONTIG},
                                 0, 3, 1, $genome_metadata->{assemblyDefault} );
  my $chr_cs = $self->get_coord_system( CS()->{CHROMOSOME},
                                        0, 1, 1,
                                        $genome_metadata->{assemblyDefault} );
  my $plasmid_cs =
    $self->get_coord_system( CS()->{PLASMID}, 0, 2, 1,
                             $genome_metadata->{assemblyDefault} );

  $genome_metadata->{coord_systems} = { CS()->{CHROMOSOME}    => $chr_cs,
                                        CS()->{MITOCHONDRION} => $chr_cs,
                                        CS()->{CHLOROPLAST}   => $chr_cs,
                                        CS()->{PLASMID}       => $plasmid_cs,
                                        CS()->{CONTIG}        => $contig_cs,
                                        CS()->{SUPERCONTIG} => $supercontig_cs,
                                        CS()->{SCAFFOLD}    => $supercontig_cs
  };

  return;
} ## end sub load_genome_data

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

sub set_sample_data {
  my ($self) = @_;
  $self->log()->info("Storing sample data");
  my @genes =
    @{ $self->dba()->get_GeneAdaptor()
      ->fetch_all_by_biotype( BIOTYPES()->{PROTEIN_CODING_GENE_TYPE} ) };

  my @named_genes = grep { defined $_->external_name() } @genes;

  if ( scalar(@named_genes) > 0 ) {
    @genes = @named_genes;
  }

  my $range = scalar @genes;
  # Create a random index.
  if ( $range > 0 ) {
    my $random_gene_index = int( rand($range) );

    # Choose random gene.
    my $random_gene = $genes[$random_gene_index];
    my $sr_name     = $random_gene->seq_region_name();
    #$sr_name =~ s/Chromosome/Chr/;
    my $sr_start = $random_gene->seq_region_start();
    my $sr_end   = $random_gene->seq_region_end();
    my $meta     = $self->dba()->get_MetaContainer;

    $self->log()
      ->debug( "Storing sample gene data " .
               ( $random_gene->external_name() || $random_gene->stable_id() ) );
    $meta->store_key_value( 'sample.location_param',
                            "$sr_name:${sr_start}-${sr_end}" );
    $meta->store_key_value( 'sample.location_text',
                            "$sr_name:${sr_start}-${sr_end}" );
    $meta->store_key_value( 'sample.gene_param', $random_gene->stable_id() );

    $meta->store_key_value( 'sample.gene_text', (
                              $random_gene->external_name() ||
                                $random_gene->stable_id() ) );
    my $transcript = @{ $random_gene->get_all_Transcripts() }[0];
    $self->log()
      ->debug( "Storing sample transcript data for " .
               ( $random_gene->external_name() || $random_gene->stable_id() ) );
    $meta->store_key_value( 'sample.transcript_param',
                            $transcript->stable_id() );
    $meta->store_key_value( 'sample.transcript_text', (
                              $transcript->external_name() ||
                                $transcript->stable_id() ) );
    $meta->store_key_value( 'sample.search_text', 'synthetase' );
  } ## end if ( $range > 0 )
  else {
    $self->log()->warn("No genes found");
  }
  $self->log()->info("Completed storing sample data");
  return;
} ## end sub set_sample_data

sub load_genome {
  my ( $self, $genome_metadata, $genome ) = @_;
  # 1. store metadata
  start_session( $self->dba(), $self->config() );
  $self->load_genome_data($genome_metadata);
  $genome_metadata->{superregnum} = $genome->{superregnum};
  # 2. store components
  my $component_loader =
    GenomeLoader::ComponentLoader->new( dba             => $self->dba(),
                                        genome_metadata => $genome_metadata,
                                        log             => $self->log(),
                                        config          => $self->config(),
                                        plugins         => $self->plugins() );

  # 2. store assembly
  $component_loader->load_assembly( $genome->{components} );

  # 3. store features
  my @hashes = ();
  for my $component ( @{ $genome->{components} } ) {
    if ( $component->{topLevel} ) {
      $self->log()->info( "Loading component " . $component->{accession} );
      my $chashes =
        $component_loader->load_features( $component, $genome_metadata );
      flush_session( $self->dba(), $self->config() );
      if ( defined $chashes && scalar(@$chashes) > 0 ) {
        @hashes = ( @hashes, @$chashes );
      }
    }
  }
  flush_session( $self->dba(), $self->config() );

  my $ctx = Digest::MD5->new();
  if ( $self->config()->{addComponents} == 1 ) {
    $ctx->add(
        $self->dba()->get_MetaContainer()->single_value_by_key('genebuild.hash')
    );
  }
  $ctx->add( sort @hashes );
  $genome_metadata->{genome_hash} = $ctx->hexdigest();

  # 4. post-process
  $self->post_process_genome($genome_metadata);
  flush_session( $self->dba(), $self->config() );

  return;
} ## end sub load_genome

sub post_process_genome {
  my ( $self, $genome_metadata ) = @_;
  $self->log()->info("Post-processing genome");
  if ( $self->config()->{addComponents} != 1 ) {
    $self->set_sample_data();
    # set repeat.analysis
    $self->log()->debug("Storing repeat.analysis entries");
    for my $anal (
      @{$self->dba()->dbc()->sql_helper()->execute_simple(
          -SQL => q/
 	select distinct(logic_name) from analysis join repeat_feature using (analysis_id)
 	join seq_region using (seq_region_id)
 	join coord_system using (coord_system_id)
 	where species_id=?
 	/,
          -PARAMS => [ $self->dba()->species_id() ] ) } )
    {
      $self->dba()->get_MetaContainer()
        ->store_key_value( 'repeat.analysis', lc($anal) );
    }
  }
  # set checksum
  $self->log()->debug("Storing genebuild hash");
  $self->dba()->get_MetaContainer()
    ->store_key_value( 'genebuild.hash', $genome_metadata->{genome_hash} );
  $self->log()->info("Completed post-processing genome");
  return;
} ## end sub post_process_genome

1;
__END__

=head1 NAME

GenomeLoader::GenomeLoader

=head1 SYNOPSIS

Module to load a specified genome and its components into a preexisting Ensembl schema.

=head1 AUTHORS

Dan Staines <dstaines@ebi.ac.uk>

=head1 METHODS

=head2 new
  Title      : new
  Description: Constructor. Invokes BaseLoader->new as well
  Args       : Hash of arguments
  Returns    : new instance

=head2 load_genome_data
  Title      : load_genome_data
  Description: load general genome data (meta table, coord systems) needed for component loading
  Args       : genome metadata hash

=head2 load_component
  Title      : load_component
  Description: load the supplied component into the current Ensembl database using GenomeLoader::ComponentLoader
  Args       : component hash

=head2 post_process_genome
  Title      : post_process_genome
  Description: run post-processing for genome (mapping paths, sample data)
  Args       : genome metadata hash

=head2 set_sample_data
  Title      : set_sample_data
  Description: set the sample data in the meta table for the current genome
  Args       : none
