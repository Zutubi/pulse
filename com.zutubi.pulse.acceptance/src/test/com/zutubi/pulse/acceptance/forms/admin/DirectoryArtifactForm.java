package com.zutubi.pulse.acceptance.forms.admin;

import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.tove.config.api.Configuration;
import com.thoughtworks.selenium.Selenium;

/**
 * The project types artifact configuration form.
 */
public class DirectoryArtifactForm extends ConfigurationForm
{
    public DirectoryArtifactForm(Selenium selenium, Class<? extends Configuration> configurationClass)
    {
        super(selenium, configurationClass);
    }

    public String getNameFieldValue()
    {
        return getFieldValue("name");
    }

    public String getBaseDirectoryFieldValue()
    {
        return getFieldValue("base");
    }

    public String getMimeTypeFieldValue()
    {
        return getFieldValue("type");
    }

    public String getIncludesFieldValue()
    {
        return getFieldValue("includes");
    }

    public String getExcludeFieldValue()
    {
        return getFieldValue("excludes");
    }

    public String[] getSelectedPostProcessorValues()
    {
        String selectedPostProcessors = getFieldValue("postprocessors");
        return selectedPostProcessors.split(",");
    }
}
