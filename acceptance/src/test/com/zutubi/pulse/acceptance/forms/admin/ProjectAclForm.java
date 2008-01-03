package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.prototype.config.project.ProjectAclConfiguration;

/**
 * Project build stage form (suits wizard too).
 */
public class ProjectAclForm extends ConfigurationForm
{
    public ProjectAclForm(Selenium selenium)
    {
        super(selenium, ProjectAclConfiguration.class);
    }

    public int[] getFieldTypes()
    {
        return new int[]{COMBOBOX, ITEM_PICKER};
    }
}
