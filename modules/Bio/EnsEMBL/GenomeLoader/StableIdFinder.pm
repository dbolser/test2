
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
#
package Bio::EnsEMBL::GenomeLoader::StableIdFinder;
use warnings;
use strict;

use Exporter 'import';
our @EXPORT_OK = qw(get_stable_id);

use Carp;
use Bio::EnsEMBL::GenomeLoader::Constants
  qw(NAMES);
use Log::Log4perl qw(get_logger);


my $get_identifying_id = sub {
  my ( $iobj ) = @_;
  return $iobj->{identifyingId};
};

my $get_public_id = sub {
  my ( $iobj ) = @_;
  return $iobj->{publicId};
};

my $get_parent_id = sub {
  my ( $iobj, $eobj, $parent, $index ) = @_;
  if ( defined $parent && defined $parent->stable_id() ) {
    return $parent->stable_id() . "-$index";
  }
  return;
};

my $id_search = {
           NAMES()->{GENE} => [ $get_identifying_id, $get_public_id ],
           NAMES()->{TRANSCRIPT} =>
             [ $get_identifying_id, $get_public_id ],
           NAMES()->{TRANSLATION} =>
             [ $get_identifying_id, $get_public_id, $get_parent_id ],
           NAMES()->{EXON} => [ $get_parent_id ] };
           
my $log = get_logger();           

sub get_id_search_for_obj {
  my ( $eobj ) = @_;
  return $id_search->{ ref($eobj) };
}

sub get_stable_id {
  my ( $iobj, $eobj, $parent, $index ) = @_;
  if ( !$iobj ) {
    return;
  }
  my $search = get_id_search_for_obj($eobj);
  if ( !$search ) {
    return;
  }
  my $stable_id;
  foreach my $id_source ( @{$search} ) {
    $stable_id = $id_source->( $iobj, $eobj, $parent, $index );
    last if $stable_id;
  }
  if ( !$stable_id ) {
    $log
      ->warn( "Cannot get stable ID for object of type " .
              ref($eobj) . " using methods " .
              join( ',', @$search ) );
  }
  return $stable_id;
}



1;
__END__

=head1 NAME

GenomeLoader::DisplayXrefFinder

=head1 SYNOPSIS

Base module to determine the most suitable xref to use for display for an ensembl gene. Uses hash of methods to call in turn for different object, retrieved by display_xrefs_search.
