package org.ensembl.genomeloader.materializer;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.ensembl.genomeloader.model.GeneName;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class GeneNameSerializer extends JsonSerializer<GeneName> {

    @Override
    public void serialize(GeneName gene, JsonGenerator gen, SerializerProvider serializers)
            throws IOException, JsonProcessingException {
        gen.writeString(gene.getName());
    }

}