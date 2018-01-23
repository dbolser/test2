=head1 LICENSE

Copyright [2009-2015] EMBL-European Bioinformatics Institute

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

=head1 NAME

Bio::EnsEMBL::GenomeLoader::Pipeline::GenomeLoaderFactory

=head1 DESCRIPTION

Simple Runnable to create a multispecies database

=head1 MAINTAINER

dstaines

=cut
package Bio::EnsEMBL::GenomeLoader::Pipeline::GenomeLoaderFactory; 

use strict;
use warnings;
use base qw/Bio::EnsEMBL::Production::Pipeline::Common::Base/;

sub run {
    my ($self) = @_;
    my $db_specs = [];
    for my $db_spec (@$db_specs) {
      
    }
    return;
}

1;