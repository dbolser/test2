
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
package Bio::EnsEMBL::GenomeLoader::ComponentLoader;
use warnings;
use strict;
use Carp;
use Bio::EnsEMBL::GenomeLoader::Utils qw(flush_session);
use Bio::EnsEMBL::GenomeLoader::SequenceLoader;
use Bio::EnsEMBL::GenomeLoader::Constants qw(BIOTYPES NAMES);
use Data::Dumper;
use base qw(Bio::EnsEMBL::GenomeLoader::BaseLoader);

sub new {
  my $caller = shift;
  my $class  = ref($caller) || $caller;
  my $self   = $class->SUPER::new(@_);
  return $self;
}

my $batchN = 25;

sub load_assembly {
  my ( $self, $genome ) = @_;
  my $seq_loader = Bio::EnsEMBL::GenomeLoader::SequenceLoader->new(-DBA=>$self->dba());
  my $sa         = $self->dba()->get_SliceAdaptor;
  my $csa        = $self->dba()->get_CoordSystemAdaptor;
  
  $self->log()->info("Loading assembly for ".$genome->{name});

  # 1. iterate over components and store them
  my $components_by_acc = {};
  for my $component ( @{$genome->{genomicComponents}} ) {
    
    $component->{metaData}{name} ||= $component->{metaData}{accession};
    
    $self->log()
      ->info(
          "Storing sequence for component " . $component->{metaData}{name} . "/" .
            $component->{accession} );
    my $slice = $seq_loader->load_sequence($component, $genome->{metaData}{assemblyDefault});
    $component->{slice} = $slice;
    if ( !$component->{topLevel} ) {
      # hash non-toplevel by accession to refer to in later assemblies
      $components_by_acc->{ $component->{accession} } = $component;
    }
    flush_session( $self->dba());
  }
  # 2. now store assembly
  my $mappings  = {};
  my $map_pairs = {};
  my $cs        = {};
  for my $component ( @{$genome->{genomicComponents}} ) {
    if ( defined $component->{assembly} &&
         scalar( @{ $component->{assembly} } ) > 0 )
    {
      $self->log()
        ->info(
          "Storing assembly for component " . $component->{metaData}{name} . "/" .
            $component->{accession} );
      for my $ass ( @{ $component->{assembly} } ) {
        my $ass_slice = $component->{slice};
        if ( !defined $ass_slice ) {
          croak "Component " . $component->{metaData}{name} . "/" .
            $component->{accession} . " does not have a slice";
        }
        my $ass_comp = $components_by_acc->{ $ass->{accession} };
        if ( !defined $ass_comp ) {
          croak "Assembly component " . $ass->{accession} .
            " not stored";
        }
        my $ass_comp_slice = $ass_comp->{slice};
        if ( !defined $ass_comp_slice ) {
          croak "Assembly component " . $ass_comp->{metaData}{name} . "/" .
            $ass_comp->{accession} . " does not have a slice";
        }
        my $map_str =
          $ass_slice->coord_system_name() . "-" .
          $ass_comp_slice->coord_system_name();
        if ( !$mappings->{$map_str} ) {
          $self->log()->info("Storing mapping for $map_str");
          $self->store_mapping_path( $ass_slice->coord_system(),
                                     $ass_comp_slice->coord_system() );
          $mappings->{$map_str} = 1;
          $cs->{ $ass_slice->coord_system_name() } =
            $ass_slice->coord_system();
          $cs->{ $ass_comp_slice->coord_system_name() } =
            $ass_comp_slice->coord_system();
          push @{ $map_pairs->{ $ass_slice->coord_system_name() } },
            $ass_comp_slice->coord_system_name();
        }
        # now create slices for both
        my $slice =
          $sa->fetch_by_region( $ass_slice->coord_system_name(),
                                $ass_slice->seq_region_name(),
                                $ass->{start},
                                $ass->{end} );
        # may need to iterate over locations
        my $ctg_slice =
          $sa->fetch_by_region( $ass_comp_slice->coord_system_name(),
                                $ass_comp_slice->seq_region_name(),
                                $ass->{location}{min},
                                $ass->{location}{max},
                                $ass->{location}{strand} );
        $self->log()
          ->info( "Storing assembly from " . $slice->name() . " to " .
                  $ctg_slice->name() );
        $sa->store_assembly( $slice, $ctg_slice );
      } ## end for my $ass ( @{ $component...})
    } ## end if ( defined $component...)
  } ## end for my $component ( @{$components...})

  # join 3 tier assemblies together
  while ( my ( $ass, $comps ) = each %$map_pairs ) {
    for my $comp (@$comps) {
      if ( defined $map_pairs->{$comp} ) {
        for my $comp3 ( @{ $map_pairs->{$comp} } ) {
          my @css = ( $cs->{$ass}, $cs->{$comp}, $cs->{$comp3} );
          my $map_str = join '-', @css;
          if ( !$mappings->{$map_str} ) {
            $self->log()->info("Storing mapping for $map_str");
            $self->store_mapping_path( $cs->{$ass}, $cs->{$comp},
                                       $cs->{$comp3} );
            $mappings->{$map_str} = 1;
          }
        }
      }
    }
  }

  $sa->_build_circular_slice_cache();

  flush_session( $self->dba() );
  return;
} ## end sub load_assembly

sub load_features {
  my ( $self, $icomponent ) = @_;

  $self->log()
    ->info( "Storing genes for component " . $icomponent->{id} );
  my $hash =
    $self->store_genes( $icomponent->{genes}, $icomponent->{slice} );
  $self->log()
    ->info( "Finished genes for component " . $icomponent->{id} );

  # Store all types of repeat feature.
  $self->log()
    ->info(
         "Storing repeat features for component " . $icomponent->{id} );
  $self->store_repeatfeatures( $icomponent->{repeatfeatures},
                               $icomponent->{slice} );

  # Store simple features.
  $self->log()
    ->info(
         "Storing simple features for component " . $icomponent->{id} );
  $self->store_simplefeatures( $icomponent->{simplefeatures},
                               $icomponent->{slice} );
  return $hash;
} ## end sub load_features

sub store_repeatfeatures {
  my ( $self, $irepeatfeatures, $slice ) = @_;
  my $cnt = 0;
  foreach my $irepeatfeature ( @{$irepeatfeatures} ) {
    $self->{plugins}->get("repeatfeature")
      ->load_feature( $irepeatfeature, $slice );
    if ( ( $cnt++ % $batchN ) == 0 ) {
      flush_session( $self->dba());
    }
  }
  flush_session( $self->dba() );
  return;
}

sub store_simplefeatures {
  my ( $self, $isimplefeatures, $slice ) = @_;
  my $sfa = $self->dba()->get_SimpleFeatureAdaptor();
  my $cnt = 0;
  foreach my $isimplefeature ( @{$isimplefeatures} ) {
    $self->{plugins}->get("simplefeature")
      ->load_feature( $isimplefeature, $slice );
    if ( ( $cnt++ % $batchN ) == 0 ) {
      flush_session( $self->dba());
    }
  }
  flush_session( $self->dba());
  return;
}

sub store_genes {
  my ( $self, $igenes, $slice ) = @_;
  my @hashes = ();
  # Store all types of gene.
  my $geneN = 0;
  if ( !defined $igenes ) {
    $self->log()->info("No genes found");
    return;
  }
  $self->log()->info( "Processing " . scalar @$igenes . " genes" );
  foreach my $igene (@$igenes) {
    eval {
      $geneN++;
      my $loader = $self->{plugins}->get( $igene->{biotype} );
      if ($loader) {
        $self->log->debug( "Loading gene with loader " . ref $loader );

        my $hash;
        my $load = sub {
          $hash = $loader->load_gene( $igene, $slice );
        };
        if ( $self->use_transactions() ) {
          $self->dba()->dbc()->sql_helper()->transaction($load);
        }
        else {
          &$load();
        }
        if ($hash) {
          push @hashes, $hash;
        }
      }
      else {
        croak( "Do not know how to load a gene of type " .
               $igene->{biotype} );
      }
    };
    if ($@) {
      my $msg =
        'Could not store ' . $igene->{biotype} . ':' .
        $igene->{ NAMES()->{PERSISTABLE_ID} } . ": $@";
      $self->log()->warn($msg);
      croak $msg;
    }
    if ( ( $geneN % $batchN ) == 0 ) {
      flush_session( $self->dba() );
    }
  } ## end foreach my $igene (@$igenes)
  flush_session( $self->dba()  );
  return \@hashes;
} ## end sub store_genes
1;
__END__

=head1 NAME

GenomeLoader::ComponentLoader

=head1 SYNOPSIS

Module to load a specified component into an ensembl database

=head1 AUTHORS

Dan Staines <dstaines@ebi.ac.uk>

=head1 METHODS

=head2 new
  Title      : new
  Description: Constructor. Invokes BaseLoader->new as well
  Args       : Hash of arguments
  Returns    : new instance

=head2 load_component
  Title      : load_component
  Description: load the specified component into an ensembl database (specified by metadata and dba attribute). Loads sequence data and also stores ensembl genes and features.
  Args       : component hash

=head2 store_genes
  Title      : store_genes
  Description: store the supplied genes in the ensembl database
  Args       : hash of genes

=head2 store_repeatfeatures
  Title      : store_repeatfeatures
  Description: store the supplied repeat features in the ensembl database
  Args       : hash of repeat features

=head2 store_simplefeatures
  Title      : store_simplefeatures
  Description: store the supplied simple features in the ensembl database
  Args       : hash of simple features




