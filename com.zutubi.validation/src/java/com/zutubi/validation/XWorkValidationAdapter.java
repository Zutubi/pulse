package com.zutubi.validation;

import java.util.*;

/**
 * Adapts from Zutubi validation aware to xwork equivalent.
 */
@SuppressWarnings({"unchecked"})
public class XWorkValidationAdapter implements ValidationAware
{
    private com.opensymphony.xwork.ValidationAware delegate;
    private boolean ignoreAllFields = false;
    private Set<String> ignoredFields = new HashSet<String>();

    public XWorkValidationAdapter(com.opensymphony.xwork.ValidationAware delegate)
    {
        this.delegate = delegate;
    }

    public void addIgnoredField(String field)
    {
        Map errors = delegate.getFieldErrors();
        errors.remove(field);
        delegate.setFieldErrors(errors);
        ignoredFields.add(field);
    }

    public void addIgnoredFields(Set<String> ignoredFields)
    {
        for(String field: ignoredFields)
        {
            addIgnoredField(field);
        }
    }

    public void ignoreAllFields()
    {
        ignoreAllFields = true;
        delegate.setFieldErrors(new HashMap());
    }

    public void addActionMessage(String message)
    {
        delegate.addActionMessage(message);
    }

    public void addActionError(String error)
    {
        delegate.addActionError(error);
    }

    public void addFieldError(String field, String error)
    {
        if (!ignoreAllFields && !ignoredFields.contains(field))
        {
            delegate.addFieldError(field, error);
        }
    }

    public Collection<String> getActionMessages()
    {
        return delegate.getActionMessages();
    }

    public Collection<String> getActionErrors()
    {
        return delegate.getActionErrors();
    }

    public List<String> getFieldErrors(String field)
    {
        return (List<String>) delegate.getFieldErrors().get(field);
    }

    public Map<String, List<String>> getFieldErrors()
    {
        return delegate.getFieldErrors();
    }

    public boolean hasErrors()
    {
        return delegate.hasErrors();
    }

    public boolean hasFieldErrors()
    {
        return delegate.hasFieldErrors();
    }

    public boolean hasFieldError(String field)
    {
        List<String> errors = getFieldErrors(field);
        if (errors != null)
        {
            return errors.size() > 0;
        }
        return false;
    }

    public boolean hasActionErrors()
    {
        return delegate.hasActionErrors();
    }

    public boolean hasActionMessages()
    {
        return delegate.hasActionMessages();
    }

    public void setActionMessages(Collection<String> messages)
    {
        delegate.setActionMessages(messages);
    }

    public void setActionErrors(Collection<String> errors)
    {
        delegate.setActionErrors(errors);
    }

    public void setFieldErrors(Map<String, List<String>> errors)
    {
        delegate.setFieldErrors(errors);
    }
}
