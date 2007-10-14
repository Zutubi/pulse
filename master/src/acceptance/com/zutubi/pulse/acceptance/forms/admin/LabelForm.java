package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.prototype.config.LabelConfiguration;

/**
 * Project label form.
 */
public class LabelForm extends ConfigurationForm
{
    public LabelForm(Selenium selenium)
    {
        super(selenium, LabelConfiguration.class);
    }
}
