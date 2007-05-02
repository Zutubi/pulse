package com.zutubi.validation;

import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.Set;

/**
 * <class-comment/>
 */
public interface ValidationAware
{
    void addIgnoredField(String field);

    void addIgnoredFields(Set<String> fields);

    void ignoreAllFields();
    
    void addActionMessage(String message);

    void addActionError(String error);

    void addFieldError(String field, String error);

    Collection<String> getActionMessages();

    Collection<String> getActionErrors();

    List<String> getFieldErrors(String field);

    Map<String, List<String>> getFieldErrors();

    boolean hasErrors();

    boolean hasFieldErrors();

    boolean hasFieldError(String field);

    boolean hasActionErrors();

    boolean hasActionMessages();

    void setActionMessages(Collection<String> messages);

    void setActionErrors(Collection<String> errors);

    void setFieldErrors(Map<String, List<String>> errors);
}
