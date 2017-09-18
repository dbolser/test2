package org.ensembl.genomeloader.materializer.impl;

public class IdentificationUncheckedException extends RuntimeException {
    
    public IdentificationUncheckedException(String message) {
        super(message);
    }


    public IdentificationUncheckedException(String message, Throwable cause) {
        super(message, cause);
    }

}
