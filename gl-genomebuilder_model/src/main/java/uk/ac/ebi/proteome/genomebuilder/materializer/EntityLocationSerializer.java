package uk.ac.ebi.proteome.genomebuilder.materializer;

import java.io.IOException;

import org.biojavax.bio.seq.RichLocation;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import uk.ac.ebi.proteome.genomebuilder.model.EntityLocation;
import uk.ac.ebi.proteome.genomebuilder.model.EntityLocationInsertion;
import uk.ac.ebi.proteome.util.biojava.LocationUtils;

public class EntityLocationSerializer extends JsonSerializer<EntityLocation> {

    protected void writeLocation(RichLocation location, JsonGenerator gen)
            throws IOException, JsonProcessingException {
        gen.writeNumberField("min", location.getMin());
        gen.writeNumberField("max", location.getMax());
        gen.writeBooleanField("min_fuzzy", location.getMinPosition().getFuzzyStart());
        gen.writeBooleanField("max_fuzzy", location.getMaxPosition().getFuzzyEnd());
        gen.writeNumberField("strand", location.getStrand().intValue());
        gen.writeNumberField("rank", location.getRank());
    }

    @Override
    public void serialize(EntityLocation location, JsonGenerator gen, SerializerProvider serializers)
            throws IOException, JsonProcessingException {
        gen.writeStartObject();
        gen.writeStringField("state", String.valueOf(location.getState()));
        writeLocation(location, gen);
        if (LocationUtils.hasInnerLocations(location)) {
            gen.writeArrayFieldStart("sublocations");
            for (RichLocation loc : LocationUtils.sortLocation(location)) {
                gen.writeStartObject();
                writeLocation(loc, gen);
                gen.writeEndObject();
            }
            gen.writeEndArray();
        }
        gen.writeArrayFieldStart("insertions");
        for (EntityLocationInsertion i : location.getInsertions()) {
            gen.writeStartObject();
            gen.writeNumberField("start", i.getStart());
            gen.writeNumberField("stop", i.getStop());
            gen.writeStringField("seq", i.getProteinSeq());
            gen.writeEndObject();
        }
        gen.writeEndArray();
        gen.writeEndObject();
    }

}