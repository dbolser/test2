
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
# Base object for returning a display_xref for a given object based on rules
# uses dynamic loading to load division specific implementations e.g. EnsemblProtists
#
package Bio::EnsEMBL::GenomeLoader::AnalysisFinder;
use warnings;
use strict;
use Carp;
use Data::Dumper;
use Bio::EnsEMBL::Analysis;
use Bio::EnsEMBL::DBSQL::BaseAdaptor;
use Bio::EnsEMBL::GenomeLoader::Utils qw(get_ensembl_dba);
use EGUtils::IO qw(parse_json);

my $sql = q/select description, display_label, data from analysis_description 
	left join web_data on (web_data_id=default_web_data_id) 
	where logic_name=?/;

my $db_names = { 'prints'       => 'PRINTS',
				 'gene3d'       => 'Gene3D',
				 'hamap'        => 'HAMAP',
				 'hmmpanther'   => 'PANTHER',
				 'pfam'         => 'Pfam',
				 'pirsf'        => 'PIRSF',
				 'blastprodom ' => 'ProDom',
				 'scanprosite'  => 'Prosite_patterns',
				 'superfamily'  => 'Superfamily',
				 'tigrfam'      => 'TIGRfam',
				 'ncoils'       => 'ncoils',
				 'pfscan'       => 'Prosite_profiles',
				 'tmhmm'        => 'Tmhmm',
				 'smart'        => 'Smart',
				 'signalp'      => 'SignalP',
				 'chainp'       => 'chainp',
				 'cdd'          => 'CDD',
				 'seg'          => 'Seq' };

sub new {
	my ( $caller, @args ) = @_;
	my $class = ref($caller) || $caller;
	my $self;
	if ( $class =~ /GenomeLoader::DisplayXrefFinder::(\S+)/x ) {
		$self = $class->SUPER::new(@args);
		$self->_initialize(@args);
	}
	else {
		$self = {};
		bless( $self, $class );
		$self->_initialize(@args);
	}
	return $self;
}

sub _initialize {
	# gets
	## dba
	## division
	## log
	## genome_metadata
	## plugins
	## config

	my ( $self, @args ) = @_;
	my %args = @args;
	$self->{config} = $args{config};
	$self->log( $args{log} );

	$self->{production_dba} = get_ensembl_dba( $self->{config}{production},
										  $self->{config}{production}{dbname} );
	$self->{file} = $self->{config}{analysisFile};
	return;
}

sub log {
	my ( $self, $log ) = @_;
	if ( defined $log ) {
		$self->{log} = $log;
	}
	return $self->{log};
}

sub get_analysis_name_map {
	my ($self) = @_;
	if ( !defined $self->{anal_name_map} ) {
		$self->{anal_name_map} = parse_json( $self->{file} );
	}
	return $self->{anal_name_map};
}

sub get_analysis_by_name {
	my ( $self, $name, $type ) = @_;
	$name = lc $name;
	my $logic_name = $self->get_analysis_name_map()->{$name};
	if ( !defined $logic_name ) {
		croak "Could not find logic_name for $name";
	}
	my $anal = $self->{anal_hash}{$logic_name};
	if ( !defined $anal ) {
		# get from production_db
		my $anal_slice =
		  $self->{production_dba}->dbc()->sql_helper()
		  ->execute( -SQL => $sql, -PARAMS => [$logic_name] );
		if ( !defined $anal_slice || scalar(@$anal_slice) == 0 ) {
			print "$sql $logic_name\n";
			print Dumper($anal_slice);
			print Dumper( $self->{production_dba}->dbc() );
			croak "Could not find analysis with logic_name $logic_name";
		}
		my ( $description, $display_label, $web_data ) = @{ $anal_slice->[0] };
		if ( defined $web_data ) {
			$web_data =
			  Bio::EnsEMBL::DBSQL::BaseAdaptor->get_dumped_data($web_data);
		}
		my $db_name = $db_names->{$logic_name};
		if ( !$db_name ) {
			$db_name = $logic_name;
		}
		if ( $db_name =~ m/ena_.*/ ) {
			$db_name = 'ena';
		}
		$anal =
		  Bio::EnsEMBL::Analysis->new(
								 -LOGIC_NAME    => $logic_name,
								 -DB            => $db_name,
								 -DESCRIPTION   => $description,
								 -DISPLAY_LABEL => $display_label,
								 -DISPLAYABLE => defined $display_label ? 1 : 0,
								 -GFF_SOURCE  => $logic_name,
								 -GFF_FEATURE => $type,
								 -WEB_DATA    => $web_data );

		$self->{anal_hash}{$logic_name} = $anal;
	} ## end if ( !defined $anal )
	if ( !defined $anal ) {
		croak
"Could not find analysis type for logic_name $logic_name (based on name $name)";
	}
	return $anal;
} ## end sub get_analysis_by_name

1;
