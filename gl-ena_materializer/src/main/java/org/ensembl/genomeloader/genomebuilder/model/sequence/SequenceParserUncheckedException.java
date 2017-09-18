package org.ensembl.genomeloader.genomebuilder.model.sequence;

public class SequenceParserUncheckedException extends RuntimeException {

    public SequenceParserUncheckedException(String message) {
        super(message);
    }

    public SequenceParserUncheckedException(Throwable cause) {
        super(cause);
    }

    public SequenceParserUncheckedException(String message, Throwable cause) {
        super(message, cause);
    }

}
