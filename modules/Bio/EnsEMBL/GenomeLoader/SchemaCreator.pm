
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

package Bio::EnsEMBL::GenomeLoader::SchemaCreator;
use warnings;
use strict;
use Carp;
use List::MoreUtils qw(uniq natatime);
use DBI;
use Bio::EnsEMBL::DBSQL::DBAdaptor;
use Bio::EnsEMBL::Utils::Argument qw( rearrange );
use Bio::EnsEMBL::Utils::CliHelper;
use base qw(Bio::EnsEMBL::GenomeLoader::BaseLoader);

sub new {
  my ( $caller, @args ) = @_;
  my $class = ref($caller) || $caller;
  my $self = $class->SUPER::new(@args);
  ( $self->{interpro_dbc} ) = rearrange( ['INTERPRO_DBC'], @args );
  return $self;
}

sub interpro_dbc {
  my ($self) = @_;
  return $self->{interpro_dbc};
}

sub create_schema {
  my ( $self, $opts ) = @_;
  $self->log()->info("Connecting to $opts->{host}");
  my $dsn = "DBI:mysql:host=$opts->{host};port=$opts->{port}";
  my $dbh = DBI->connect( $dsn, $opts->{user}, $opts->{pass} ) ||
    croak "Could not connect to $dsn: $!";

  $dbh->do( 'DROP DATABASE IF EXISTS ' . $opts->{dbname} );
  $dbh->do( 'CREATE DATABASE ' . $opts->{dbname} );
  $dbh->do( 'USE ' . $opts->{dbname} );

  # create schema from base of Ensembl
  my $mod = 'Bio/EnsEMBL/ApiVersion.pm';
  ( my $loc = $INC{$mod} ) =~ s/modules\/${mod}$//;
  my $sql = $loc . 'sql/table.sql';
  $self->log()->debug("Loading SQL from $sql as InnoDB");
  system(
"sed -e 's/MyISAM/InnoDB/' $sql | mysql -u$opts->{user} -p$opts->{pass} -h$opts->{host} -P$opts->{port} $opts->{dbname}"
  );
  my ($dba) =
    @{ Bio::EnsEMBL::Utils::CliHelper->new()->get_dbas_for_opts($opts) };
  $self->{dba} = $dba;
  $self->populate_tables();
  $self->log()->info( $opts->{dbname} . " ready for use" );
  return $dba;
} ## end sub create_schema

sub populate_tables {
  my ($self) = @_;
  if ( !defined $self->production_dba() ) {
    $self->log()
      ->warn("Production database not specified - cannot populate tables");
    return;
  }
  $self->log()->info("Populating controlled tables");
  my $ph = $self->production_dba()->dbc()->sql_helper();
  my $h  = $self->dba()->dbc()->sql_helper();

  # load controlled tables from production
  my $tables = {
    misc_set => [qw/misc_set_id code name description max_length/],
    unmapped_reason =>
      [qw/unmapped_reason_id summary_description full_description/],
    external_db => [
      qw/external_db_id db_name db_release status priority db_display_name type secondary_db_name secondary_db_table description/
    ],
    attrib_type => [qw/attrib_type_id code  name description/],
    biotype => [qw/biotype_id name object_type db_type attrib_type_id description biotype_group so_acc/]
     };
  while ( my ( $table, $columns ) = each %$tables ) {
    $self->log()->debug("Loading controlled table $table");
    $ph->execute_no_return(
      -SQL => "SELECT " .
        join( ',', @$columns ) . " FROM master_$table WHERE is_current=1",
      -CALLBACK => sub {
        my ($row) = @_;
        $h->execute_update(
                    -SQL => "INSERT INTO $table(" .
                      join( ',', @$columns ) . ") VALUES(" .
                      join( ',', map( '?', ( 1 .. scalar(@$columns) ) ) ) . ")",
                    -PARAMS => $row );

        return;
      } );
  }
  return;
} ## end sub populate_tables

sub finish_schema {
  my ($self) = @_;
  $self->log()
    ->info( "Running post-load steps on " . $self->dba()->dbc()->dbname() );
  $self->clean_versions();
  $self->load_interpro();
  $self->clean_analysis();
  $self->update_analysis_descriptions();
  $self->log()
    ->info(
       "Finished running post-load steps on " . $self->dba()->dbc()->dbname() );
  return;
}

sub clean_versions {
  my ($self) = @_;
  $self->log()->info("Removing blank version for coord_system");
  $self->dba()->dbc()->sql_helper()
    ->execute_update( -SQL => q/update coord_system set version=NULL where version=''/ );
  $self->log()->info("Removing version 0 for xrefs");
  $self->dba()->dbc()->sql_helper()
    ->execute_update( -SQL => q/update xref set version=NULL where version=0/ );
  $self->log()->info("Removing spurious version for features");
  for my $table (qw/gene transcript translation exon/) {
    $self->dba()->dbc()->sql_helper()
      ->execute_update(
         -SQL => qq/update $table set version=NULL where version is not null/ );
  }
  return;
}

sub load_interpro {
  my ($self) = @_;

  if ( !defined $self->interpro_dbc() ) {
    $self->log()->info("Interpro DBC not set - skipping");
    return;
  }

  $self->log()->info("Loading Interpro data");

  my $ih = $self->interpro_dbc()->sql_helper();
  my $h  = $self->dba()->dbc()->sql_helper();

  # get list of InterPro
  my %interpro = map { $_ => 1 }
    @{
    $h->execute_simple(
      -SQL =>
"select dbprimary_acc from xref join external_db using (external_db_id) where db_name = 'Interpro'"
    ) };

  $self->log()
    ->debug( "Found " . scalar( keys %interpro ) . " InterPro xrefs" );

  # get list of distinct hit_ac
  my %hit_ac = map { $_ => 1 }
    @{
    $h->execute_simple( -SQL => "select distinct hit_name from protein_feature"
    ) };

  $self->log()->debug("Removing old interpro data");
  $h->execute_update( -SQL => "truncate table interpro" );

  my $it = natatime 1000, keys %interpro;

  my @hits;
  $self->log()->debug("Retrieving method mappings");
  while ( my @vals = $it->() ) {
    my $method_query =
      'select entry_ac,method_ac from interpro.entry2method where entry_ac in ('
      . join( ',', map { "'$_'" } @vals ) . ")";

    $ih->execute_no_return(
      -SQL      => $method_query,
      -CALLBACK => sub {
        my ($row) = @_;
        if ( defined $hit_ac{ $row->[1] } ) {
          push @hits, "('" . $row->[0] . "','" . $row->[1] . "')";
        }
        return;
      } );
  }

  $self->log()->debug( "Storing " . scalar(@hits) . " InterPro mappings" );
  $it = natatime 1000, @hits;
  while ( my @vals = $it->() ) {
    my $ensembl_query =
      "INSERT IGNORE INTO interpro (interpro_ac, id) VALUES " .
      join( ',', @vals );
    $h->execute_update( -SQL => $ensembl_query );
  }

  $self->log()->debug("Finding extra InterPro xrefs");

  # get all interpro details to supplement what we have
  my @iprs = ();
  $ih->execute_no_return(
    -SQL => "select distinct entry_ac, short_name, name from interpro.entry",
    -CALLBACK => sub {
      my ($row) = @_;
      if ( !defined $interpro{ $row->[0] } ) {
        push @iprs,
          '(1200,"' . $row->[0] . '","' . $row->[1] . '","' . $row->[2] . '")';
        $interpro{ $row->[0] } = 1;
      }
      return;
    } );

  $self->log()->debug( "Storing " . scalar(@iprs) . " extra InterPro xrefs" );
  $it = natatime 1000, @iprs;
  while ( my @ipr = $it->() ) {
    my $ensembl_query =
"INSERT IGNORE INTO xref(external_db_id,dbprimary_acc,display_label,description) VALUES "
      . join( ',', @ipr );
    $h->execute_update( -SQL => $ensembl_query );
  }

  return;
} ## end sub load_interpro

sub clean_analysis {
  my ($self) = @_;
  $self->log()->info("Cleaning up density types");
  $self->dba()->dbc()->sql_helper()
    ->execute_update( -SQL =>
q/delete dt.* from density_type dt left join density_feature df using (density_type_id) where df.density_type_id is null/
    );
  $self->log()->info("Cleaning up analysis types");
  my %valid_analysis_ids = ();
  for my $table ( 'data_file',             'density_type',
                  'ditag_feature',         'dna_align_feature',
                  'gene',                  'intron_supporting_evidence',
                  'marker_feature',        'object_xref',
                  'operon',                'operon_transcript',
                  'prediction_transcript', 'protein_align_feature',
                  'protein_feature',       'repeat_feature',
                  'simple_feature',        'transcript',
                  'unmapped_object' )
  {
    for my $anal_id ( @{$self->dba()->dbc()->sql_helper()->execute_simple(
                              -SQL => "select distinct(analysis_id) from $table"
                        ) } )
    {
      $valid_analysis_ids{$anal_id} = 1;
    }
  }
  my $sql =
"delete a.*,ad.* from analysis a left join analysis_description ad using (analysis_id) where analysis_id not in ("
    . join( ',', keys %valid_analysis_ids ) . ")";
  $self->log()->debug("Executing $sql");
  $self->dba()->dbc()->sql_helper()->execute_update( -SQL => $sql );
  return;
} ## end sub clean_analysis

sub update_analysis_descriptions {
  my ($self) = @_;
  if ( !defined $self->production_dba() ) {
    $self->log()->info("Production DBA not set - skipping");
    return;
  }
  $self->log()->info("Updating analysis descriptions");
  my $h  = $self->dba()->dbc()->sql_helper();
  my $ph = $self->production_dba()->dbc()->sql_helper();
  for my $row (
     @{ $h->execute( -SQL => 'select analysis_id,logic_name from analysis' ) } )
  {
    $self->log()->debug( "Updating analysis description for " . $row->[1] );
    for my $des (
      @{$ph->execute(
          -SQL => q/select ad.description, ad.display_label, w.data 
      from analysis_description ad 
      left join web_data w on (w.web_data_id=ad.default_web_data_id) 
      where logic_name=?/,
          -PARAMS => [ $row->[1] ] ) } )
    {
      $h->execute_update(
        -SQL => q/
        insert ignore into analysis_description(analysis_id, description, display_label, web_data)
        values(?,?,?,?)/,
        -PARAMS => [ $row->[1], $des->[0], $des->[1], $des->[2] ] );
    }
  }
  return;
} ## end sub update_analysis_descriptions

1;
