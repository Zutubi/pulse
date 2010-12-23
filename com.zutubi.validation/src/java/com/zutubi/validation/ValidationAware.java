package com.zutubi.validation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface implemented by objects that are aware of validation processing,
 * and interested in the validation results. 
 */
public interface ValidationAware
{
    /**
     * Indicates that the specified field is being ignored by the validation.
     *
     * @param field name
     */
    void addIgnoredField(String field);

    /**
     * Indicates that the set of fields is being ignored by the validation.
     *
     * @param fields names
     */
    void addIgnoredFields(Set<String> fields);

    /**
     * Indicates that all fields are being ignored by validation.
     */
    void ignoreAllFields();

    /**
     * Indicates that a general validation error has been detected.  These
     * errors are not tied to individual fields.
     *
     * @param error message
     */
    void addActionError(String error);

    /**
     * Indicates that a field error has been detected.
     *
     * @param field name
     * @param error message
     */
    void addFieldError(String field, String error);

    /**
     * Return the collection of action errors that have been recorded.
     *
     * @return a collection of error messages.
     */
    Collection<String> getActionErrors();

    /**
     * Return the list of field errors that have been recorded.
     *
     * @param field name
     *
     * @return list of error messages.
     */
    List<String> getFieldErrors(String field);

    /**
     * Return the map of field name to error message list.
     *
     * @return field / error mapping.
     */
    Map<String, List<String>> getFieldErrors();

    /**
     * @return true if any errors have been detected.
     */
    boolean hasErrors();

    /**
     * @return true if any field errors have been detected.
     */
    boolean hasFieldErrors();

    /**
     * @param field name
     * @return true if an error has been detected for the named field.
     */
    boolean hasFieldError(String field);

    /**
     * @return true if any action errors have been detected.
     */
    boolean hasActionErrors();

    /**
     * Indicate that all recorded errors should be reset.  This will typically be used
     * if the object is reused for multiple validations
     */
    void clearFieldErrors();
}
