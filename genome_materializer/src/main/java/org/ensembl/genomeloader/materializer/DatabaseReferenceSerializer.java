package org.ensembl.genomeloader.materializer;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.ensembl.genomeloader.model.DatabaseReference;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class DatabaseReferenceSerializer extends JsonSerializer<DatabaseReference> {

    public static void writeStringFieldIfNotEmpty(JsonGenerator gen, String field, String value) throws IOException {
        if (!StringUtils.isEmpty(value))
            gen.writeStringField(field, value);
    }

    @Override
    public void serialize(DatabaseReference ref, JsonGenerator gen, SerializerProvider serializers)
            throws IOException, JsonProcessingException {

        gen.writeStartObject();
        gen.writeStringField("primaryIdentifier", ref.getPrimaryIdentifier());
        writeStringFieldIfNotEmpty(gen, "secondaryIdentifier", ref.getSecondaryIdentifier());
        writeStringFieldIfNotEmpty(gen, "tertiaryIdentifier", ref.getTertiaryIdentifier());
        writeStringFieldIfNotEmpty(gen, "quarternaryIdentifier", ref.getQuarternaryIdentifier());
        if(ref.getDatabaseReferenceType().getEnsemblName()==null) {
            throw new NullPointerException("Null ensemblName for "+ref);
        }
           
        gen.writeStringField("databaseReferenceType", ref.getDatabaseReferenceType().getEnsemblName());
        if (ref.isIdentityXref()) {
            gen.writeBooleanField("isIdentityXref", ref.isIdentityXref());
            gen.writeNumberField("identity", ref.getIdentity().doubleValue());
        }
        if (ref.getSource() != null)
            gen.writeObjectField("source", ref.getSource());
        gen.writeEndObject();

    }

}