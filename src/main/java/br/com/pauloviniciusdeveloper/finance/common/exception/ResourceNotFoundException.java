package br.com.pauloviniciusdeveloper.finance.common.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resource, Object id) {
        super("%s not found: %s".formatted(resource, id), HttpStatus.NOT_FOUND);
    }
}
