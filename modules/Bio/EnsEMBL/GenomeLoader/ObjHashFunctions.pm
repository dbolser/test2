
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
# Module collecting utilities for use in loading genomes
#
package Bio::EnsEMBL::GenomeLoader::ObjHashFunctions;
use warnings;
use strict;
use Carp;
use Digest::MD5 qw(md5_base64);
use Exporter 'import';
our @EXPORT_OK =
  qw(get_gene_hash get_type_analysis_hash get_location_hash get_gene_xref_hash get_gene_display_xref_hash);
my $HASH_SEP = ';';

sub get_gene_hash {
  my ( $feat, $hash ) = @_;
  $hash =
    build_hash( $feat->external_name(), $feat->description(),
                get_type_analysis_hash( $feat, $hash ) );
  foreach my $ts ( sort { $_[0]->start() <=> $_[1]->start() }
                   @{ $feat->get_all_Transcripts() } )
  {
    $hash = get_transcript_hash( $ts, $hash );
  }
  return $hash;
}

sub get_gene_xref_hash {
  my ( $feat, $hash ) = @_;
  return get_xref_hash( $feat, get_gene_display_xref_hash($feat) );
}

sub get_gene_display_xref_hash {
  my ( $feat, $hash ) = @_;
  return
    build_hash( $feat->display_xref()->primary_id(),
                get_type_analysis_hash( $feat, $hash ) );
}

sub get_xref_hash {
  my ( $feat, $hash ) = @_;
  my @dbs = ();
  foreach my $dbe ( @{ $feat->get_all_DBEntries() } ) {
    #push @dbs, $dbe->database().':'.$dbe->primary_id();
    push @dbs, $dbe->primary_id();
  }
  my $db_hash = build_hash( sort @dbs );
  return build_hash( $hash, $db_hash );
}

sub get_type_analysis_hash {
  my ( $feat, $hash ) = @_;
  return
    build_hash( $feat->biotype(),
                $feat->analysis()->logic_name(),
                get_location_hash( $feat, $hash ) );
}

sub get_transcript_hash {
  my ( $feat, $hash ) = @_;
  $hash = get_location_hash( $feat, $hash );
  my $translation = $feat->translation();
  if ($translation) {
    $hash = build_hash( md5_base64( $translation->seq() ), $hash );
  }
  return $hash;
}

sub get_location_hash {
  my ( $feat, $hash ) = @_;
  return build_hash( $feat->start(), $feat->end(), $feat->strand(),
                     $hash );
}

sub build_hash {
  my @hash_vals = ();
  foreach my $hash_val (@_) {
    if ($hash_val) {
      push @hash_vals, $hash_val;
    }
  }
  return join( $HASH_SEP, @hash_vals );
}

1;
