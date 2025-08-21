package com.eojeda89.decididorapi.common.exception;

/**
 * Custom exceptions used across the application.
 */
public final class Exceptions {
    private Exceptions() {}

    public static class InvalidRequestException extends RuntimeException {
        public InvalidRequestException(String message) { super(message); }
    }

    public static class UnsupportedAlgorithmException extends RuntimeException {
        public UnsupportedAlgorithmException(String message) { super(message); }
    }

    public static class AlgorithmFailureException extends RuntimeException {
        public AlgorithmFailureException(String message) { super(message); }
        public AlgorithmFailureException(String message, Throwable cause) { super(message, cause); }
    }

    public static class DomainValidationException extends RuntimeException {
        public DomainValidationException(String message) { super(message); }
    }

    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) { super(message); }
    }

    public static class ConflictException extends RuntimeException {
        public ConflictException(String message) { super(message); }
    }

    public static class AlgorithmException extends RuntimeException {
        public AlgorithmException(String message, Throwable cause) { super(message, cause); }
    }
}
