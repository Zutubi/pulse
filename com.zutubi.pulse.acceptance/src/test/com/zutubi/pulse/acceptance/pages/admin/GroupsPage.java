/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.acceptance.pages.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.admin.BuiltinGroupForm;
import com.zutubi.pulse.acceptance.forms.admin.GroupForm;
import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.GROUPS_SCOPE;
import com.zutubi.pulse.master.webwork.Urls;
import org.openqa.selenium.By;

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
        browser.click(By.id(ADD_LINK));
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
