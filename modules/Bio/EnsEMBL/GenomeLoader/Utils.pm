
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
use English qw(-no_match_vars);
use Log::Log4perl qw(get_logger);
use JSON;
use File::Spec;
use JSON;
use Bio::EnsEMBL::DBSQL::DBAdaptor;
use Scalar::Util qw(openhandle blessed);
use Exporter 'import';
our @EXPORT_OK =
  qw(get_ensembl_dba get_database_connection from_json_file slurp_file from_json_file_default start_session flush_session println slurp quote parse_json write_out process_dir);

my $log = get_logger();


sub get_ensembl_dba {
	# Get Ensembl DB adaptor.
	my ( $ens_config, $schema_name, $species_id ) = @_;
	my %args = ( -DBNAME => $schema_name,
				 -HOST   => $ens_config->{host},
				 -USER   => $ens_config->{user},
				 -PASS   => $ens_config->{pass},
				 -PORT   => $ens_config->{port}, );
	if ( defined($species_id) && $schema_name =~ m/_collection_core_/ ) {
		$args{-MULTISPECIES_DB} = 1;
		$args{-SPECIES_ID}      = $species_id;
	}
	return Bio::EnsEMBL::DBSQL::DBAdaptor->new(%args) or
	  croak "Could not connect to database for $schema_name";
}

sub start_session {
	my ( $dba, $config ) = @_;
	if ( $config->{ensembl}{engine} eq 'InnoDB' ) {
		$log->debug("Starting session");
		$dba->dbc()->db_handle()->{'AutoCommit'} = 0;
	}
	return;
}

sub flush_session {
	my ( $dba, $config ) = @_;
	if ( $config->{ensembl}{engine} eq 'InnoDB' ) {
		$log->debug("Flushing session");
		$dba->dbc()->db_handle()->commit();
	}
	return;
}

sub get_database_connection {
	my ($conf) = @_;
	my $connstr;
	if ( $conf->{driver} eq 'Oracle' ) {
		$connstr = "DBI:Oracle:host=" .
		  $conf->{host} . ";sid=" . $conf->{sid} . ";port=" . $conf->{port};
	}
	else {
		$connstr =
		  'DBI:' . $conf->{driver} .
		  ':' . $conf->{schema} . '@' . $conf->{host} . ':' . $conf->{port};
	}
	return DBI->connect( $connstr, $conf->{user}, $conf->{pass} ) or
	  croak 'Could not connect to database';
}

sub from_json_file_default {
	my ( $filename, $default ) = @_;
	if ( !$default ) { $default = {}; }
	my $o = $default;
	if ( -e $filename ) { $o = parse_json($filename); }
	return $o;
}
sub println {
	my $possible_fh = select();
	my $handle;
	{
		## no critic (ProhibitNoStrict)
		no strict 'refs';
		$handle = (openhandle($_[0])) ? shift @_ : \*$possible_fh;
		## use critic
	}

	@_ = $_ unless @_;

	if(blessed($handle)) {
		return $handle->print(@_, $RS);
	}
	else {
		return print {$handle} @_, $RS;
	}
}

sub slurp {
  my $in = shift @_;
  my $as_ref = shift @_;
  my $return_ref;
  if(openhandle($in)) {
    $return_ref = _slurp_fh($in);
  }
  else {
    open(my $fh, '<', $in) or confess "Cannot open '${in}' for reading: $!";
    $return_ref = _slurp_fh($fh);
    close($fh);
  }

	return $return_ref if($as_ref);
  return ${$return_ref};
}

sub write_out {
	my ($in, $closure) = @_;
	if(openhandle($in)) {
		$closure->($in);
	}
	else {
		open(my $fh, '>', $in) or confess "Cannot open '${in}' for writing: $!";
		$closure->($fh);
		close($fh);
	}
}

sub _slurp_fh {
  my $fh = shift @_;
  {
    local $/ = undef;
    my $content = <$fh>;
    return \$content;
  }
}

sub quote {
	return sprintf('"%s"', shift @_);
}

sub parse_json {
	my ($incoming, $relaxed_parsing) = @_;
	my $data_ref;
	#If it looks like a file(_handle) then treat it as such
	if(!defined $incoming) {
		confess('Undefined reference given');
	}
	elsif( openhandle($incoming) || ($incoming !~ /\n/ && -f $incoming) ) {
		$data_ref = slurp($incoming, 1);
	}
	else {
		$data_ref = \$incoming;
	}

	my $json = JSON->new();
	$json->relaxed(1) if $relaxed_parsing;
	return $json->decode(${$data_ref});
}

sub process_dir {
	my ($dir, $closure, $include_unix_relative) = @_;

	opendir my $dh, $dir or confess "Couldn't open dir '$dir': $!";

	my @files = grep {
		my ($file, $ok) = ($_, 1);
		if(!$include_unix_relative) {
			$ok = 0 if ($file eq '.' || $file eq '..');
		}
		$ok;
	} readdir $dh;

	closedir $dh or confess "Could not close dir '$dir' : $!";

	if($closure) {
		foreach my $file (@files) {
			if($closure) {
				$closure->($file);
			}
		}
	}
	else {
		return @files;
	}
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
 
 =head2 slurp()

	my $ref = slurp('file_loc', 1); #Slurp in file location & return as a scalar::ref
	print ${$ref}, "\n";

Takes in a file location or open file handle, reads the value into a scalar
and returns the scalar. Fastest way of getting a file into memory. Can return
a reference to the scalar if you the data is going to be big.

=head2 write_out()

	write_out('/tmp/file.txt', sub {
		my ($fh) = @_;
		print $fh "Hello there\n";
	});

Works in a similar way to slurp in that you can give it a file handle or a
path & the program attempts to open the file for writing & then gives you
the file handle back. You then carry out your writing in the closure &
then if you didn't open the file handle let the subroutine deal with the
closing process.

=head2 println()

	println('Hello'); #Goes to the default output
	println($fh, 'Hello'); #Prints it to the $fh

Probably the main reason you are here. Ends each print statement with a
record separator ($RS from English). Works in the same way print does
(except you cannot call it on a file handle, the handle must be passed in).

=head2 quote()

Quotes a given string with single quotes i.e. q becomes 'q'

=head2 parse_json()

	#Returns a json derrived array containing 3 elements
	my $array = parse_json('[1,2,3]');
	my $hash = parse_json('/some/file');
	my $hash = parse_json($fh);

	#Or
	my $hash = parse_json(q([1,2,3,]), 1); #Note the trailing , in the JSON array

This method will take in a JSON data set in a scalar, a file location or a
file handle and will return the parsed data structure.

It is also possible to switch on a more relaxed parsing of JSON by giving a
second true boolean parameter. This version allows comments and trailing commas
which the JSON serialization specification disallows but you may want if you
are using JSON for configuration reasons. For more information see C<JSON>'s
POD.

=head2 process_dir()

	my $include_relative = 0;
	process_dir('/tmp/mydir', sub {println $_;}, $include_relative);

Performs a readdir on the given directory and then if a closure is given will
invoke this on each entry found in the dir. This lets you process each hit
(testing to see if they are a directory or not & creating full paths)
rather than assuming you want an array returned.

Invoking without a closure (or an undef in its place) will cause an arrayref
of found entries to be returned.

The final boolean argument will force the inclusion of . & .. in the output
array (normally we strip this out).
 