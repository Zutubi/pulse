package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.SeleniumForm;
import com.zutubi.pulse.prototype.config.project.types.AntTypeConfiguration;

/**
 */
public class AntTypeForm extends SeleniumForm
{
    public AntTypeForm(Selenium selenium)
    {
        super(selenium);
    }

    public String getFormName()
    {
        return AntTypeConfiguration.class.getName();
    }

    public String[] getFieldNames()
    {
        return new String[]{ "work", "file", "target", "args", "postProcessors" };
    }

    public int[] getFieldTypes()
    {
        return new int[] { TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD, MULTI_SELECT };
    }
}
