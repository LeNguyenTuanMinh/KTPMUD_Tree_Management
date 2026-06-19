package com.beepollen.exception;

/**
 * Exception thrown when a requested resource is not found in the system.
 * Produces a descriptive message indicating the resource type, field, and value
 * that was used in the lookup.
 */
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    /**
     * Constructs a new ResourceNotFoundException.
     *
     * @param resourceName the name of the resource (e.g., "User", "Plant")
     * @param fieldName    the field used for the lookup (e.g., "id", "username")
     * @param fieldValue   the value of the field that was not found
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("Resource not found: %s with %s = %s", resourceName, fieldName, fieldValue));
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
