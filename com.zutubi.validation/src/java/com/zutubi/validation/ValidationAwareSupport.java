package com.zutubi.validation;

import java.util.*;

/**
 * <class-comment/>
 */
public class ValidationAwareSupport implements ValidationAware
{
    private Set<String> ignoredFields;
    private Collection<String> actionErrors;
    private Collection<String> actionMessages;
    private Map<String, List<String>> fieldErrors;
    private boolean ignoreAllFields = false;

    public void addIgnoredField(String field)
    {
        internalGetFieldErrors().remove(field);
        getIgnoredFields().add(field);
    }

    public void addIgnoredFields(Set<String> fields)
    {
        for(String field: fields)
        {
            addIgnoredField(field);
        }
    }

    public void ignoreAllFields()
    {
        fieldErrors = null;
        ignoreAllFields = true;
    }

    private Set<String> getIgnoredFields()
    {
        if(ignoredFields == null)
        {
            ignoredFields = new HashSet<String>();
        }
        return ignoredFields;
    }

    public void addActionError(String error)
    {
        internalGetActionErrors().add(error);
    }

    public void addActionMessage(String message)
    {
        internalGetActionMessages().add(message);
    }

    public void addFieldError(String field, String error)
    {
        if (!ignoreAllFields && !getIgnoredFields().contains(field))
        {
            Map<String, List<String>> errors = internalGetFieldErrors();
            if (!errors.containsKey(field))
            {
                errors.put(field, new LinkedList<String>());
            }
            List<String> aFieldsErrors = errors.get(field);
            aFieldsErrors.add(error);
        }
    }

    public Collection<String> getActionErrors()
    {
        return new LinkedList<String>(internalGetActionErrors());
    }

    public Collection<String> getActionMessages()
    {
        return new LinkedList<String>(internalGetActionMessages());
    }

    public List<String> getFieldErrors(String field)
    {
        return internalGetFieldErrors().get(field);
    }

    public boolean hasErrors()
    {
        return hasActionErrors() || hasFieldErrors();
    }

    public boolean hasFieldErrors()
    {
        return internalGetFieldErrors().size() > 0;
    }

    public boolean hasActionErrors()
    {
        return internalGetActionErrors().size() > 0;
    }

    public boolean hasActionMessages()
    {
        return internalGetActionErrors().size() > 0;
    }

    public boolean hasFieldError(String field)
    {
        List<String> errors = getFieldErrors(field);
        return (errors != null) && errors.size() > 0;
    }

    public Map<String, List<String>> getFieldErrors()
    {
        return new HashMap<String, List<String>>(internalGetFieldErrors());
    }

    public void setActionMessages(Collection<String> messages)
    {
        this.actionMessages = messages;
    }

    public void setActionErrors(Collection<String> errors)
    {
        this.actionErrors = errors;
    }

    public void setFieldErrors(Map<String, List<String>> errors)
    {
        this.fieldErrors = errors;
    }

    private Collection<String> internalGetActionMessages()
    {
        if (actionMessages == null)
        {
            actionMessages = new LinkedList<String>();
        }
        return actionMessages;
    }

    private Collection<String> internalGetActionErrors()
    {
        if (actionErrors == null)
        {
            actionErrors = new LinkedList<String>();
        }
        return actionErrors;
    }

    private Map<String, List<String>> internalGetFieldErrors()
    {
        if (fieldErrors == null)
        {
            fieldErrors = new HashMap<String, List<String>>();
        }
        return fieldErrors;
    }
}
