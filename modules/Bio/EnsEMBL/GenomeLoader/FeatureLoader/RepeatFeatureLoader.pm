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
package Bio::EnsEMBL::GenomeLoader::FeatureLoader::RepeatFeatureLoader;
use warnings;
use strict;
use Bio::EnsEMBL::RepeatConsensus;
use Bio::EnsEMBL::RepeatFeature;
use Data::Dumper;
use base qw(GenomeLoader::FeatureLoader);

sub new {
	my $caller = shift;
	my $class  = ref($caller) || $caller;
	my $self   = $class->SUPER::new(@_);
	$self->{aa}  = $self->dba()->get_AnalysisAdaptor();
	$self->{rfa} = $self->dba()->get_RepeatFeatureAdaptor();
	return $self;
}

sub load_feature {
	my ( $self, $irepeatfeature, $slice ) = @_;
	# Consensus.
	my $irepeatUnit = $irepeatfeature->{repeatUnit};

	my $name = $irepeatUnit->{repeatName}||$irepeatUnit->{repeatClass};
	$name =~ s/'/\\'/g;
	
	my $analysis = lc($irepeatfeature->{analysis});
	my $type;
	eval {
		$type = $self->analysis_finder()->get_analysis_by_name($analysis,"repeat");
	};
	if($@) {
		$self->log()->debug("Could not find repeat type with name $name - using ena_repeat");	
		$type = $self->analysis_finder()->get_analysis_by_name("repeat","repeat");
	}

	my $econsensus =
	  Bio::EnsEMBL::RepeatConsensus->new(
						   -NAME   => $name,
						   -LENGTH => length( $irepeatUnit->{repeatConsensus} ),
						   -REPEAT_CLASS     => $irepeatUnit->{repeatClass},
						   -REPEAT_CONSENSUS => $irepeatUnit->{repeatConsensus},
						   -REPEAT_TYPE      => $irepeatUnit->{repeatType} );
	# store analysis track if not yet stored (not necessary for other features)
	if ( !$type->dbID() ) {
		$self->{aa}->store($type);
	}
	my $ilocation = $irepeatfeature->{location};
	my $erepeatfeature =
	  Bio::EnsEMBL::RepeatFeature->new(
									  -REPEAT_CONSENSUS => $econsensus,
									  -HSTART => $irepeatfeature->{repeatStart},
									  -HEND   => $irepeatfeature->{repeatEnd},
									  -SCORE  => $irepeatfeature->{score},
									  -START  => $ilocation->{min},
									  -END    => $ilocation->{max},
									  -STRAND => $ilocation->{strand},
									  -ANALYSIS => $type,
									  -SLICE    => $slice );
	$self->{rfa}->store($erepeatfeature);
	return $erepeatfeature;
} ## end sub load_feature
1;
__END__

=head1 NAME

GenomeLoader::FeatureLoader::RepeatFeatureLoader

=head1 SYNOPSIS

Load repeat features into an ensembl database.

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
  Returns    : Bio::EnsEMBL::RepeatConsensus
