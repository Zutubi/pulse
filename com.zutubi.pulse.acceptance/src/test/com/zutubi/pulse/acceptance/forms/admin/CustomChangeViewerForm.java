package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.master.tove.config.project.changeviewer.CustomChangeViewerConfiguration;

/**
 */
public class CustomChangeViewerForm extends ConfigurationForm
{
    public CustomChangeViewerForm(Selenium selenium)
    {
        super(selenium, CustomChangeViewerConfiguration.class);
    }
}
