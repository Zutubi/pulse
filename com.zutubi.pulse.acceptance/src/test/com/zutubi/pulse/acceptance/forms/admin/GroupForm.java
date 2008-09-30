package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.tove.config.group.GroupConfiguration;

/**
 * User group add and edit form.
 */
public class GroupForm extends ConfigurationForm
{
    public GroupForm(Selenium selenium)
    {
        super(selenium, GroupConfiguration.class);
    }

    public int[] getFieldTypes()
    {
        return new int[] { TEXTFIELD, ITEM_PICKER, MULTI_SELECT };
    }
}
