
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
package Bio::EnsEMBL::GenomeLoader::Utils;
use warnings;
use strict;
use Carp;
use Log::Log4perl qw(get_logger);
use Exporter 'import';
our @EXPORT_OK =
  qw(start_session flush_session disable_transactions);

my $log = get_logger();

sub start_session {
  my ( $dba ) = @_;
    $log->debug("Starting session");
    $dba->dbc()->db_handle()->{'AutoCommit'} = 0;
  return;
}

sub flush_session {
  my ( $dba ) = @_;
    $log->debug("Flushing session");
    $dba->dbc()->db_handle()->commit();
  return;
}

sub disable_transactions {
  my ( $dba ) = @_;
    $log->debug("Disabling transactions");
  $dba->dbc()->db_handle()->{'AutoCommit'} = 1;
  return;
}

1;
