package com.zutubi.validation;

import java.util.List;

/**
 * <class-comment/>
 */
public interface ValidationAware
{
    void addActionError(String error);

    void addFieldError(String field, String error);

    List<String> getActionErrors();

    List<String> getFieldErrors(String field);

    boolean hasErrors();

    boolean hasFieldErrors();

    boolean hasActionErrors();
}
