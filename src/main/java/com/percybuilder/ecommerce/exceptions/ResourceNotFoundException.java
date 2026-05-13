package com.percybuilder.ecommerce.exceptions;

import org.apache.coyote.BadRequestException;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}

