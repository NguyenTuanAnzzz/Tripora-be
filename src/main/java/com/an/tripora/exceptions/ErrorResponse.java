package com.an.tripora.exceptions;

public record ErrorResponse(
        int status,
        String message,
        String path
) {
}