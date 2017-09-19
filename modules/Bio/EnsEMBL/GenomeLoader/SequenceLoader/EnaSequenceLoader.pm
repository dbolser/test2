
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

# $Source$# $Revision$
# $Date$
# $Author$
#
# Object for loading data into an ensembl database

package Bio::EnsEMBL::GenomeLoader::SequenceLoader::EnaSequenceLoader;
use warnings;
use strict;
use Carp;
use Bio::EnsEMBL::GenomeLoader::Constants qw(CS);
use base qw(GenomeLoader::SequenceLoader);

sub new {
  my $caller = shift;
  my $class  = ref($caller) || $caller;
  my $self   = $class->SUPER::new(@_);
  if ( !$self->genome_metadata() ) {
    croak("Genome metadata not supplied");
  }
  return $self;
}

sub parse_description {
  my ( $self, $icomponent ) = @_;
  my $component_name        = $icomponent->{description};
  my $component_name_prefix = CS()->{SUPERCONTIG};

  # possibilities
  # 1. Chromosome - bacterial or eukaryotic
  # 2. Plasmid
  # 3. Mitochondrion
  # 4. Chloroplast
  # 5. WGS
  # 6. Unknown

  if ( $component_name =~ /mitochondri/i ) {
    $component_name_prefix = CS()->{MITOCHONDRION};
    $component_name        = 'MT';
  }
  elsif ( $component_name =~ /chloroplast/i ) {
    $component_name_prefix = CS()->{CHLOROPLAST};
    $component_name        = 'PT';
  }
  elsif ( $component_name =~ /plasmid ([^ ,]+)/i ) {
    $component_name_prefix = CS()->{PLASMID};
    $component_name        = $1;
  }
  elsif ( $component_name =~ /([^ ]+) genomic scaffold/i ||
         $component_name =~ /([^ ,]+), whole genome shotgun sequence/i )
  {
    $component_name_prefix = CS()->{SUPERCONTIG};
    $component_name        = $1;
  }
  elsif ( $component_name =~ /chromosome ([^ ,]+)/i ) {
    $component_name_prefix = CS()->{CHROMOSOME};
    $component_name        = $1;
  }
  elsif ( $component_name =~ /complete genome/i &&
          ( $self->genome_metadata()->{lineage}->[-1] eq 'Archaea' ||
            $self->genome_metadata()->{lineage}->[-1] eq 'Bacteria' ) )
  {
    $component_name_prefix = CS()->{CHROMOSOME};
    $component_name        = CS()->{CHROMOSOME};
  }
  else {
    $component_name = $icomponent->{accession};
    $self->log()
      ->warn( "Cannot parse component " . $icomponent->{description} .
              " - using $component_name_prefix $component_name" );
  }

  return ( $component_name_prefix, $component_name );
} ## end sub parse_description

1;
__END__

=head1 NAME

GenomeLoader::SequenceLoader::EnaSequenceLoader

=head1 SYNOPSIS

Module to load a specified component sequence into an ensembl database

=head1 AUTHORS

Dan Staines <dstaines@ebi.ac.uk>

=head1 METHODS

=head2 new
  Title      : new
  Description: Constructor. Invokes SequenceLoader->new as well
  Args       : Hash of arguments
  Returns    : new instance

=head2 parse_description
  Title      : load_sequence
  Description: create coord system for the supplied sequence and store the sequence into the ensembl database.
  Args       : component hash


