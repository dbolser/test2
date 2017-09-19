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
use JSON;
use Bio::EnsEMBL::DBSQL::DBAdaptor;
use EGUtils::IO qw(parse_json);
use Exporter 'import';
our @EXPORT_OK =
  qw(get_ensembl_dba get_database_connection from_json_file slurp_file from_json_file_default start_session flush_session);
my $log = get_logger("");

sub get_ensembl_dba {
	# Get Ensembl DB adaptor.
	my ( $ens_config, $schema_name, $species_id ) = @_;
	my %args = (
		-DBNAME => $schema_name,
		-HOST   => $ens_config->{host},
		-USER   => $ens_config->{user},
		-PASS   => $ens_config->{pass},
		-PORT   => $ens_config->{port},
	);
	if ( defined($species_id) && $schema_name =~ m/_collection_core_/ ) {
		$args{-MULTISPECIES_DB} = 1;
		$args{-SPECIES_ID}      = $species_id;
	}
	return Bio::EnsEMBL::DBSQL::DBAdaptor->new(%args)
	  or croak "Could not connect to database for $schema_name";
}


sub start_session {
	my ($dba,$config) = @_;
	if($config->{ensembl}{engine} eq 'InnoDB') {
		$log->debug("Starting session");
		$dba->dbc()->db_handle()->{'AutoCommit'} = 0;
	}
	return;
}
sub flush_session {
	my ($dba,$config) = @_;
	if($config->{ensembl}{engine} eq 'InnoDB') {
		$log->debug("Flushing session");
		$dba->dbc()->db_handle()->commit();
	}
	return;
}

sub get_database_connection {
	my ($conf) = @_;
	my $connstr;
	if ( $conf->{driver} eq 'Oracle' ) {
		$connstr =
		    "DBI:Oracle:host="
		  . $conf->{host} . ";sid="
		  . $conf->{sid}
		  . ";port="
		  . $conf->{port};
	} else {
		$connstr = 'DBI:'
		  . $conf->{driver} . ':'
		  . $conf->{schema} . '@'
		  . $conf->{host} . ':'
		  . $conf->{port};
	}
	return DBI->connect( $connstr, $conf->{user}, $conf->{pass} )
	  or croak 'Could not connect to database';
}

sub from_json_file_default {
	my ( $filename, $default ) = @_;
	if ( !$default ) { $default = {}; }
	my $o = $default;
	if ( -e $filename ) { $o = parse_json($filename); }
	return $o;
}

1;
__END__

=head1 NAME

GenomeLoader::Utils - collection of utilties for loading genomes

=head1 SYNOPSIS

A package containing miscellaneous utilities

=head1 AUTHORS

Dan Staines <dstaines@ebi.ac.uk>

=head1 METHODS

=head2 connect
  Title      : get_database_connection
  Description: Get database connection for an arbitrary database
  Args       : database config hash
  Returns    : database connection

=head2 get_ensembl_dba
  Title      : get_ensembl_dba
  Description: Get ensembl database adaptor.
  Args       : database config hash, schema_name, species ID (optional)
  Returns    : database adaptor

=head2 from_json_file_default
  Title      : from_json_file_default
  Description: Parse the contents of a file containing json into a Perl variable, returning a default value if the file is not found
  Args       : filename, default
  Returns    : variable containing parsed data
