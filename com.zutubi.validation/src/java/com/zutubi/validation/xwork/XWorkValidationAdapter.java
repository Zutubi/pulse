package com.zutubi.validation.xwork;

import com.zutubi.validation.ValidationAware;

import java.util.*;

/**
 * Adapts from Zutubi validation aware to xwork equivalent.
 */
@SuppressWarnings({"unchecked"})
public class XWorkValidationAdapter implements ValidationAware
{
    private com.opensymphony.xwork.ValidationAware delegate;
    private String fieldSuffix = "";
    private boolean ignoreAllFields = false;
    private Set<String> ignoredFields = new HashSet<String>();

    public XWorkValidationAdapter(com.opensymphony.xwork.ValidationAware delegate)
    {
        this.delegate = delegate;
    }

    public XWorkValidationAdapter(com.opensymphony.xwork.ValidationAware delegate, String fieldSuffix)
    {
        this.delegate = delegate;
        this.fieldSuffix = fieldSuffix;
    }

    public void addIgnoredField(String field)
    {
        Map errors = delegate.getFieldErrors();
        errors.remove(field + fieldSuffix);
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

    public void addActionError(String error)
    {
        delegate.addActionError(error);
    }

    public void addFieldError(String field, String error)
    {
        if (!ignoreAllFields && !ignoredFields.contains(field))
        {
            delegate.addFieldError(field + fieldSuffix, error);
        }
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
        return errors != null && errors.size() > 0;
    }

    public boolean hasActionErrors()
    {
        return delegate.hasActionErrors();
    }

    public void clearFieldErrors()
    {
        delegate.setFieldErrors(null);
    }
}
