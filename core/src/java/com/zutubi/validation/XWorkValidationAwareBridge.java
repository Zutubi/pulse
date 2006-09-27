package com.zutubi.validation;

import com.opensymphony.xwork.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * <class-comment/>
 */
public class XWorkValidationAwareBridge implements ValidationAware
{
    private com.opensymphony.xwork.ValidationAware delegate;

    public XWorkValidationAwareBridge(com.opensymphony.xwork.ValidationAware delegate)
    {
        this.delegate = delegate;
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
        delegate.addFieldError(field, error);
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
