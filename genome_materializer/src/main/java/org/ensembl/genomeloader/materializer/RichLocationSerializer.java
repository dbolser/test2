package org.ensembl.genomeloader.materializer;

import java.io.IOException;

import org.biojavax.bio.seq.RichLocation;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class RichLocationSerializer extends JsonSerializer<RichLocation> {

	@Override
	public void serialize(RichLocation location, JsonGenerator gen, SerializerProvider serializers)
			throws IOException, JsonProcessingException {
		gen.writeStartObject();
		gen.writeNumberField("min", location.getMin());
		gen.writeNumberField("max", location.getMax());
		gen.writeBooleanField("min_fuzzy", location.getMinPosition().getFuzzyStart());
		gen.writeBooleanField("max_fuzzy", location.getMaxPosition().getFuzzyEnd());
		gen.writeNumberField("strand", location.getStrand().intValue());
		gen.writeNumberField("rank", location.getRank());
		gen.writeEndObject();
	}

}