
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
package Bio::EnsEMBL::GenomeLoader::FeatureLoader;
use warnings;
use strict;
use Carp;
use Data::Dumper;
use base qw(Bio::EnsEMBL::GenomeLoader::BaseLoader);

sub new {
  my $caller = shift;
  my $class  = ref($caller) || $caller;
  my $self   = $class->SUPER::new(@_);
  return $self;
}

sub load_feature {
  my ( $self, $irepeat, $slice ) = @_;
  croak("store_feature cannot be invoked on the base class");
}
1;
__END__

=head1 NAME

GenomeLoader::FeatureLoader

=head1 SYNOPSIS

Base module to load a specified feature hash into an ensembl database. Specific implementations needed for specific feature type.#

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
  Description: Create feature on supplied slice from hash. Empty implementation - must be overridden.
  Args       : Repeat hash, slice to store on
