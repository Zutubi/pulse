package com.zutubi.pulse.acceptance.forms.admin;

import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.tove.config.group.UserGroupConfiguration;

/**
 * User group add and edit form.
 */
public class GroupForm extends ConfigurationForm
{
    public GroupForm(SeleniumBrowser browser)
    {
        super(browser, UserGroupConfiguration.class);
    }

    public int[] getFieldTypes()
    {
        return new int[] { TEXTFIELD, ITEM_PICKER, MULTI_SELECT };
    }
}
