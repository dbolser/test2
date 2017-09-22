
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
  qw(start_session flush_session);

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
 
