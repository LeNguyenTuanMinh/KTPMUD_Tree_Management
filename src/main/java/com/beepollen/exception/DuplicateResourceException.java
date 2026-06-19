package com.beepollen.exception;

/**
 * Exception thrown when attempting to create a resource that already exists.
 * Used for uniqueness constraint violations such as duplicate usernames or emails.
 */
public class DuplicateResourceException extends RuntimeException {

    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    /**
     * Constructs a new DuplicateResourceException.
     *
     * @param resourceName the name of the resource (e.g., "User")
     * @param fieldName    the field that has a duplicate value (e.g., "username", "email")
     * @param fieldValue   the duplicate value
     */
    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("Duplicate resource: %s already exists with %s = %s", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }
}
