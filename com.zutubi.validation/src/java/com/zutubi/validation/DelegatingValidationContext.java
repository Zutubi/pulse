/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.validation;

import com.zutubi.util.config.PropertiesConfig;
import com.zutubi.validation.i18n.DefaultTextProvider;
import com.zutubi.validation.i18n.TextProvider;
import com.zutubi.validation.xwork.XWorkValidationAdapter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <class-comment/>
 */
public class DelegatingValidationContext implements ValidationContext
{
    protected ValidationAware validationAware;
    protected TextProvider textProvider;
    protected PropertiesConfig config = new PropertiesConfig();

    public DelegatingValidationContext(ValidationAware validationAware, TextProvider textProvider)
    {
        this.validationAware = validationAware;
        this.textProvider = textProvider;
    }

    public DelegatingValidationContext(Object obj)
    {
        validationAware = makeValidationAware(obj);
        textProvider = makeTextProvider(obj);
    }

    protected DelegatingValidationContext()
    {

    }

    public TextProvider makeTextProvider(Object obj)
    {
        if (obj instanceof TextProvider)
        {
            return ((TextProvider)obj).getTextProvider(obj);
        }
        return new DefaultTextProvider();
    }

    public ValidationAware makeValidationAware(Object o)
    {
        if (o instanceof ValidationAware)
        {
            return (ValidationAware)o;
        }
        else if (o instanceof com.opensymphony.xwork.ValidationAware)
        {
            return new XWorkValidationAdapter((com.opensymphony.xwork.ValidationAware)o);
        }
        return new ValidationAwareSupport();
    }

    public TextProvider getTextProvider(Object context)
    {
        return textProvider.getTextProvider(context);
    }

    public void addActionError(String error)
    {
        validationAware.addActionError(error);
    }

    public void addFieldError(String field, String error)
    {
        validationAware.addFieldError(field, error);
    }

    public Collection<String> getActionErrors()
    {
        return validationAware.getActionErrors();
    }

    public List<String> getFieldErrors(String field)
    {
        return validationAware.getFieldErrors(field);
    }

    public boolean hasErrors()
    {
        return validationAware.hasErrors();
    }

    public boolean hasFieldErrors()
    {
        return validationAware.hasFieldErrors();
    }

    public boolean hasActionErrors()
    {
        return validationAware.hasActionErrors();
    }

    public void clearFieldErrors()
    {
        validationAware.clearFieldErrors();
    }

    public boolean hasFieldError(String field)
    {
        return validationAware.hasFieldError(field);
    }

    public Map<String, List<String>> getFieldErrors()
    {
        return validationAware.getFieldErrors();
    }

    public void addIgnoredField(String field)
    {
        validationAware.addIgnoredField(field);
    }

    public void addIgnoredFields(Set<String> fields)
    {
        validationAware.addIgnoredFields(fields);
    }

    public void ignoreAllFields()
    {
        validationAware.ignoreAllFields();
    }

    public String getText(String key)
    {
        return textProvider.getText(key);
    }

    public String getText(String key, Object... args)
    {
        return textProvider.getText(key, args);
    }

    public String getProperty(String key)
    {
        return config.getProperty(key);
    }

    public void setProperty(String key, String value)
    {
        config.setProperty(key, value);
    }

    public boolean hasProperty(String key)
    {
        return config.hasProperty(key);
    }

    public void removeProperty(String key)
    {
        config.removeProperty(key);
    }

    public boolean isWritable()
    {
        return config.isWritable();
    }

    @Override
    public boolean shouldIgnoreValidator(Validator validator)
    {
        return false;
    }
}
