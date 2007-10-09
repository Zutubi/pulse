package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.SeleniumForm;
import com.zutubi.pulse.prototype.config.project.ProjectAclConfiguration;

/**
 * Project build stage form (suits wizard too).
 */
public class ProjectAclForm extends SeleniumForm
{
    public ProjectAclForm(Selenium selenium)
    {
        super(selenium);
    }

    public String getFormName()
    {
        return ProjectAclConfiguration.class.getName();
    }

    public String[] getFieldNames()
    {
        return new String[]{ "group", "allowedActions" };
    }

    public int[] getFieldTypes()
    {
        return new int[]{COMBOBOX, ITEM_PICKER};
    }
}
