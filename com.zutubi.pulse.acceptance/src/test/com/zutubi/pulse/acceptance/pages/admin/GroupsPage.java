package com.zutubi.pulse.acceptance.pages.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.admin.BuiltinGroupForm;
import com.zutubi.pulse.acceptance.forms.admin.GroupForm;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.GROUPS_SCOPE;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * Simple specialisation of a list page for the admin/groups tab.
 */
public class GroupsPage extends ListPage
{
    public GroupsPage(SeleniumBrowser browser, Urls urls)
    {
        super(browser, urls, GROUPS_SCOPE);
    }

    public GroupForm clickAddGroupAndWait()
    {
        browser.click(ADD_LINK);
        GroupForm form = browser.createForm(GroupForm.class);
        form.waitFor();
        return form;
    }

    public GroupForm clickViewGroupAndWait(String baseName)
    {
        clickView(baseName);
        GroupForm form = browser.createForm(GroupForm.class);
        form.waitFor();
        return form;
    }

    public BuiltinGroupForm clickViewBuiltinGroupAndWait(String baseName)
    {
        clickView(baseName);
        BuiltinGroupForm form = browser.createForm(BuiltinGroupForm.class);
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
