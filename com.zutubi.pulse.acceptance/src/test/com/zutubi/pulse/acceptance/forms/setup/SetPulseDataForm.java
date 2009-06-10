package com.zutubi.pulse.acceptance.forms.setup;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.SeleniumForm;
import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.tove.config.setup.SetupDataConfiguration;

/**
 */
public class SetPulseDataForm extends SeleniumForm
{
    public SetPulseDataForm(SeleniumBrowser browser)
    {
        super(browser, false);
    }

    public String getFormName()
    {
        return SetupDataConfiguration.class.getName();
    }

    public String[] getFieldNames()
    {
        return new String[]{"data"};
    }
}
