
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

Bio::EnsEMBL::GenomeLoader::Pipeline::FinishDatabase

=head1 DESCRIPTION

Simple Runnable to run final steps to complete loading of a multispecies database

=head1 MAINTAINER

dstaines

=cut

package Bio::EnsEMBL::GenomeLoader::Pipeline::FinishDatabase;
use strict;
use warnings;
use base qw/Bio::EnsEMBL::Production::Pipeline::Common::Base/;

sub run {
  my ($self) = @_;

  my $interpro_dbc;
  my $config_file = $self->param('config_file');

  if ( defined $config_file ) {
    my $cfg = read_file($config_file);
    if ( $cfg =~
m/.*<interproUri>.*jdbc:oracle:thin:([^\/]+)\/([^@]+)@\/\/([^:]+):([0-9]+)\/([A-Z0-9]+).*<\/interproUri>.*/
      )
    {
      $self->log()->info("Connecting to Interpro database");
      $interpro_dbc =
        Bio::EnsEMBL::DBSQL::DBConnection->new( -USER   => $1,
                                                -PASS   => $2,
                                                -HOST   => $3,
                                                -PORT   => $4,
                                                -DBNAME => $5,
                                                -DRIVER => 'Oracle' );
    }
  }

  my $schema =
    Bio::EnsEMBL::GenomeLoader::SchemaCreator->new(
                                     -PRODUCTION_DBA => $self->production_dba(),
                                     -INTERPRO_DBC   => $interpro_dbc );

  my $dba =
    Bio::EnsEMBL::DBSQL::DBAdaptor->new( -host   => $self->param('host'),
                                         -port   => $self->param('port'),
                                         -user   => $self->param('user'),
                                         -pass   => $self->param('pass'),
                                         -dbname => $self->param('dbname') );

  $schema->dba($dba);
  $self->info()->info("Finishing schema");
  $schema->finish_schema();

} ## end sub run

1;
