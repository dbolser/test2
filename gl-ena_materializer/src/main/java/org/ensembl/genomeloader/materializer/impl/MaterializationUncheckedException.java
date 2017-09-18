package org.ensembl.genomeloader.materializer.impl;

public class MaterializationUncheckedException extends RuntimeException {
    
    public MaterializationUncheckedException(String message) {
        super(message);
    }


    public MaterializationUncheckedException(String message, Throwable cause) {
        super(message, cause);
    }

}
