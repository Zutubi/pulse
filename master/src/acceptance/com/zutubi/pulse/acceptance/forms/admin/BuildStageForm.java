package com.zutubi.pulse.acceptance.forms.admin;

import com.zutubi.pulse.acceptance.forms.SeleniumForm;
import com.zutubi.pulse.prototype.config.LabelConfiguration;
import com.zutubi.pulse.prototype.config.project.BuildStageConfiguration;
import com.thoughtworks.selenium.Selenium;

/**
 * Project build stage form (suits wizard too).
 */
public class BuildStageForm extends SeleniumForm
{
    public BuildStageForm(Selenium selenium, boolean inherited)
    {
        super(selenium, true, inherited);
    }

    public String getFormName()
    {
        return BuildStageConfiguration.class.getName();
    }

    public String[] getFieldNames()
    {
        return new String[]{ "name", "recipe", "agent" };
    }

    public int[] getFieldTypes()
    {
        return new int[]{TEXTFIELD, TEXTFIELD, COMBOBOX};
    }
}
