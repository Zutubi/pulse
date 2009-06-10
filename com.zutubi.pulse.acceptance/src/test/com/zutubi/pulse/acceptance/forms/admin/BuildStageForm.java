package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;

/**
 * Project build stage form (suits wizard too).
 */
public class BuildStageForm extends ConfigurationForm
{
    public BuildStageForm(SeleniumBrowser browser, boolean inherited)
    {
        super(browser, BuildStageConfiguration.class, true, inherited);
    }

    public int[] getFieldTypes()
    {
        return new int[]{TEXTFIELD, TEXTFIELD, COMBOBOX};
    }
}
