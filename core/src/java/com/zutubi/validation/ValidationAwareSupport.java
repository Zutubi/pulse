package com.zutubi.validation;

import java.util.List;
import java.util.Map;
import java.util.LinkedList;
import java.util.HashMap;

/**
 * <class-comment/>
 */
public class ValidationAwareSupport implements ValidationAware
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
        List<String> aFieldsErrors = fieldErrors.get(field);
        aFieldsErrors.add(error);
    }

    public List<String> getActionErrors()
    {
        return actionErrors;
    }

    public List<String> getFieldErrors(String field)
    {
        return fieldErrors.get(field);
    }

    public boolean hasErrors()
    {
        return hasActionErrors() || hasFieldErrors();
    }

    public boolean hasFieldErrors()
    {
        return fieldErrors.size() > 0;
    }

    public boolean hasActionErrors()
    {
        return actionErrors.size() > 0;
    }

    public Map<String, List<String>> getFieldErrors()
    {
        return new HashMap<String, List<String>>(fieldErrors);
    }
}
