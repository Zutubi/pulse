package com.zutubi.pulse.form.validator;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

/**
 * <class-comment/>
 */
public class ValidatorContextSupport implements ValidatorContext
{
    private List<String> actionErrors = new LinkedList<String>();

    private Map<String, List<String>> fieldErrors = new HashMap<String, List<String>>();

    public void addActionError(String error)
    {
        actionErrors.add(error);
    }

    public void addFieldError(String field, String error)
    {
        if (!fieldErrors.containsKey(field))
        {
            fieldErrors.put(field, new LinkedList<String>());
        }
        List<String> errors = fieldErrors.get(field);
        errors.add(error);
    }

    public boolean hasErrors()
    {
        return hasActionErrors() || hasFieldErrors();
    }

    public boolean hasFieldErrors()
    {
        return !fieldErrors.isEmpty();
    }

    public boolean hasActionErrors()
    {
        return !actionErrors.isEmpty();
    }

    public List<String> getActionErrors()
    {
        return new LinkedList<String>(actionErrors);
    }

    public List<String> getFieldErrors(String field)
    {
        List<String> errors = new LinkedList<String>();
        if (fieldErrors.containsKey(field))
        {
            errors.addAll(fieldErrors.get(field));
        }
        return errors;
    }

    public String getText(String key)
    {
        return key;
    }
}
