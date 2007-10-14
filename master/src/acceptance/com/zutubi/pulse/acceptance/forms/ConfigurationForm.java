package com.zutubi.pulse.acceptance.forms;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.config.annotations.Form;
import com.zutubi.pulse.core.config.Configuration;

/**
 * Abstract base for forms that are based off a configuration class, where
 * the class is available (i.e. not a plugin).  The form and field names are
 * automatically determined from the class.
 */
public abstract class ConfigurationForm extends SeleniumForm
{
    private Class<? extends Configuration> configurationClass;

    protected ConfigurationForm(Selenium selenium, Class<? extends Configuration> configurationClass)
    {
        super(selenium);
        this.configurationClass = configurationClass;
    }

    protected ConfigurationForm(Selenium selenium, Class<? extends Configuration> configurationClass, boolean ajax)
    {
        super(selenium, ajax);
        this.configurationClass = configurationClass;
    }

    public ConfigurationForm(Selenium selenium, Class<? extends Configuration> configurationClass, boolean ajax, boolean inherited)
    {
        super(selenium, ajax, inherited);
        this.configurationClass = configurationClass;
    }

    public String getFormName()
    {
        return configurationClass.getName();
    }

    public String[] getFieldNames()
    {
        return configurationClass.getAnnotation(Form.class).fieldOrder();
    }
}
