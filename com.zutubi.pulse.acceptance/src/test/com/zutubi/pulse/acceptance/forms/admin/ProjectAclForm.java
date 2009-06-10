package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.tove.config.project.ProjectAclConfiguration;

/**
 * Project build stage form (suits wizard too).
 */
public class ProjectAclForm extends ConfigurationForm
{
    public ProjectAclForm(SeleniumBrowser browser)
    {
        super(browser, ProjectAclConfiguration.class);
    }

    public int[] getFieldTypes()
    {
        return new int[]{COMBOBOX, ITEM_PICKER};
    }
}
