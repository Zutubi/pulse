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

package com.zutubi.tove.config;

import com.zutubi.tove.config.api.Configuration;
import com.zutubi.util.config.Config;
import com.zutubi.util.config.PropertiesConfig;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.Validator;
import com.zutubi.validation.i18n.TextProvider;
import com.zutubi.validation.validators.RequiredValidator;

import java.util.*;

/**
 * A specialised validation context that carries extra information specific
 * to the Pulse configuration system.
 */
public class ConfigurationValidationContext implements ValidationContext
{
    private Configuration instance;
    private TextProvider textProvider;
    private String parentPath;
    private String baseName;
    private boolean template;
    private boolean checkEssential;
    private boolean ignoreAllFields = false;
    private Set<String> ignoredFields = new HashSet<String>();
    private Config config = new PropertiesConfig();

    private ConfigurationTemplateManager configurationTemplateManager;

    public ConfigurationValidationContext(Configuration instance, TextProvider textProvider, String parentPath, String baseName, boolean template, boolean checkEssential, ConfigurationTemplateManager configurationTemplateManager)
    {
        this.instance = instance;
        this.textProvider = textProvider;
        this.parentPath = parentPath;
        this.baseName = baseName;
        this.template = template;
        this.checkEssential = checkEssential;
        this.configurationTemplateManager = configurationTemplateManager;
    }

    /**
     * @return the instance to be validated
     */
    public Configuration getInstance()
    {
        return instance;
    }

    /**
     * @return the parent path of the object being validated
     */
    public String getParentPath()
    {
        return parentPath;
    }

    /**
     * @return the base name of the path of the object being validated, which
     *         may be null if the object is new 
     */
    public String getBaseName()
    {
        return baseName;
    }

    /**
     * @return true if the required validator should be ignored (as is the
     *         case when validating a template)
     */
    public boolean isTemplate()
    {
        return template;
    }

    public boolean isCheckEssential()
    {
        return checkEssential;
    }

    public void addIgnoredField(String field)
    {
        ignoredFields.add(field);
        instance.clearFieldErrors(field);
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
        ignoreAllFields = true;
        instance.clearFieldErrors();
    }

    public ConfigurationTemplateManager getConfigurationTemplateManager()
    {
        return configurationTemplateManager;
    }

    public void addActionError(String error)
    {
        instance.addInstanceError(error);
    }

    public void addFieldError(String field, String error)
    {
        if(!ignoreAllFields && !ignoredFields.contains(field))
        {
            instance.addFieldError(field, error);
        }
    }

    public Collection<String> getActionErrors()
    {
        return instance.getInstanceErrors();
    }

    public List<String> getFieldErrors(String field)
    {
        return instance.getFieldErrors(field);
    }

    public Map<String, List<String>> getFieldErrors()
    {
        return instance.getFieldErrors();
    }

    public boolean hasErrors()
    {
        return !instance.isValid();
    }

    public boolean hasFieldErrors()
    {
        return getFieldErrors().size() > 0;
    }

    public boolean hasFieldError(String field)
    {
        return getFieldErrors(field).size() > 0;
    }

    public boolean hasActionErrors()
    {
        return getActionErrors().size() > 0;
    }

    public void clearFieldErrors()
    {
        instance.clearFieldErrors();
    }

    public String getText(String key)
    {
        return textProvider.getText(key);
    }

    public String getText(String key, Object... args)
    {
        return textProvider.getText(key, args);
    }

    public TextProvider getTextProvider(Object context)
    {
        return textProvider.getTextProvider(context);
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
        if (template)
        {
            if (validator instanceof RequiredValidator)
            {
                return true;
            }
        }

        return false;
    }
}
