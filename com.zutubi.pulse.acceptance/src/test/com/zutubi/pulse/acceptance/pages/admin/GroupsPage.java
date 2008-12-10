package com.zutubi.pulse.acceptance.pages.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.pulse.master.tove.config.ConfigurationRegistry;
import com.zutubi.pulse.acceptance.forms.admin.GroupForm;
import com.zutubi.pulse.acceptance.forms.admin.BuiltinGroupForm;

/**
 * Simple specialisation of a list page for the admin/groups tab.
 */
public class GroupsPage extends ListPage
{
    public GroupsPage(Selenium selenium, Urls urls)
    {
        super(selenium, urls, ConfigurationRegistry.GROUPS_SCOPE);
    }

    public GroupForm clickAddGroupAndWait()
    {
        selenium.click(ADD_LINK);
        GroupForm form = new GroupForm(selenium);
        form.waitFor();
        return form;
    }

    public GroupForm clickViewGroupAndWait(String baseName)
    {
        clickView(baseName);
        GroupForm form = new GroupForm(selenium);
        form.waitFor();
        return form;
    }

    public BuiltinGroupForm clickViewBuiltinGroupAndWait(String baseName)
    {
        clickView(baseName);
        BuiltinGroupForm form = new BuiltinGroupForm(selenium);
        form.waitFor();
        return form;
    }

    public boolean isGroupPresent(String groupName)
    {
        try
        {
            int i = 0;
            while (true)
            {
                String locatedGroupName = getCellContent(i, 0);
                if (locatedGroupName.equals(groupName))
                {
                    return true;
                }
                i++;
            }
        }
        catch (Exception e)
        {
            return false;
        }
    }
}
