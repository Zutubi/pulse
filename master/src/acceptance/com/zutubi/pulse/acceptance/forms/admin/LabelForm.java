package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.SeleniumForm;
import com.zutubi.pulse.prototype.config.LabelConfiguration;

/**
 * Project label form.
 */
public class LabelForm extends SeleniumForm
{
    public LabelForm(Selenium selenium)
    {
        super(selenium);
    }

    public String getFormName()
    {
        return LabelConfiguration.class.getName();
    }

    public String[] getFieldNames()
    {
        return new String[]{ "label" };
    }
}
