package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.acceptance.Constants;
import com.zutubi.pulse.master.tove.config.project.types.DirectoryArtifactConfiguration;

/**
 * The project types artifact configuration form.
 */
public class DirectoryArtifactForm extends ConfigurationForm
{
    public DirectoryArtifactForm(Selenium selenium)
    {
        super(selenium, DirectoryArtifactConfiguration.class);
    }

    public String getNameFieldValue()
    {
        return getFieldValue(Constants.DirectoryArtifact.NAME);
    }

    public String getBaseDirectoryFieldValue()
    {
        return getFieldValue(Constants.DirectoryArtifact.BASE);
    }

    public String getMimeTypeFieldValue()
    {
        return getFieldValue(Constants.DirectoryArtifact.MIME_TYPE);
    }

    public String getIncludesFieldValue()
    {
        return getFieldValue(Constants.DirectoryArtifact.INCLUDES);
    }

    public String getExcludeFieldValue()
    {
        return getFieldValue(Constants.DirectoryArtifact.EXCLUDES);
    }

    public String[] getSelectedPostProcessorValues()
    {
        return convertMultiValue(getFieldValue(Constants.DirectoryArtifact.POSTPROCESSORS));
    }
}
