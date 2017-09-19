
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
package Bio::EnsEMBL::GenomeLoader::FeatureLoader::SimpleFeatureLoader;
use warnings;
use strict;
use Bio::EnsEMBL::SimpleFeature;
use base qw(GenomeLoader::FeatureLoader);

sub new {
  my $caller = shift;
  my $class  = ref($caller) || $caller;
  my $self   = $class->SUPER::new(@_);
  $self->{sfa} = $self->dba()->get_SimpleFeatureAdaptor();
  return $self;
}

sub load_feature {
  my ( $self, $isimplefeature, $slice ) = @_;
  if ( !$isimplefeature->{analysis} ) {
    $isimplefeature->{analysis} = $isimplefeature->{featureType};
  }

  my $name = lc( $isimplefeature->{analysis} );
  $name =~ s/[^0-9a-z_]+//g;
  my $eanalysis =
    $self->analysis_finder()->get_analysis_by_name( $name, 'feature' );

  my $ilocation = $isimplefeature->{location};
  my $label     = $isimplefeature->{displayLabel};
  if ( length($label) > 255 ) {
    $label = substr( $label, 0, 252 ) . '...';
  }
  my $esimplefeature =
    Bio::EnsEMBL::SimpleFeature->new(-START    => $ilocation->{min},
                                     -END      => $ilocation->{max},
                                     -STRAND   => $ilocation->{strand},
                                     -SLICE    => $slice,
                                     -ANALYSIS => $eanalysis,
                                     -SCORE => $isimplefeature->{score},
                                     -DISPLAY_LABEL => $label );
  $self->{sfa}->store($esimplefeature);
  return $esimplefeature;
} ## end sub load_feature
1;
__END__

=head1 NAME

GenomeLoader::FeatureLoader::SimpleFeatureLoader

=head1 SYNOPSIS

Load simple features into an ensembl database.

=head1 AUTHORS

Dan Staines <dstaines@ebi.ac.uk>

=head1 METHODS

=head2 new
  Title      : new
  Description: Constructor. Invokes BaseLoader->new as well
  Args       : Hash of arguments
  Returns    : new instance

=head2 load_feature
  Title      : new
  Description: Create repeat feature and consensus on supplied slice from hash.
  Args       : repeat hash, slice
  Returns    : Bio::EnsEMBL::SimpleFeature
