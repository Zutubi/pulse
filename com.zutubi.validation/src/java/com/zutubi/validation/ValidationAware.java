package com.zutubi.validation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <class-comment/>
 */
public interface ValidationAware
{
    void addIgnoredField(String field);

    void addIgnoredFields(Set<String> fields);

    void ignoreAllFields();
    
    void addActionError(String error);

    void addFieldError(String field, String error);

    Collection<String> getActionErrors();

    List<String> getFieldErrors(String field);

    Map<String, List<String>> getFieldErrors();

    boolean hasErrors();

    boolean hasFieldErrors();

    boolean hasFieldError(String field);

    boolean hasActionErrors();

    void clearFieldErrors();
}
