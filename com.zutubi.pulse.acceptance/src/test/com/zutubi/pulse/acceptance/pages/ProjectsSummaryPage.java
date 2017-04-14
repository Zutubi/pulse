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

package com.zutubi.pulse.acceptance.pages;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.webwork.Urls;
import org.openqa.selenium.By;

import java.util.LinkedList;
import java.util.List;

/**
 * Base class for pages that show projects with their latest status (e.g. the
 * browse and dashboard views).
 */
public abstract class ProjectsSummaryPage extends SeleniumPage
{
    public static final String ACTION_HIDE = "hide";

    public ProjectsSummaryPage(SeleniumBrowser browser, Urls urls, String id, String title)
    {
        super(browser, urls, id, title);
    }

    public boolean isUngroupedProjectPresent(String name)
    {
        return browser.isElementIdPresent(getProjectRowId(null, name));
    }

    public boolean isProjectPresent(String group, String name)
    {
        return browser.isElementIdPresent(getProjectRowId(group, name));
    }

    public boolean isGroupPresent(String group)
    {
        return browser.isElementIdPresent(getGroupId(group));
    }

    public String getProjectMenuId(String group, String project)
    {
        return getProjectRowId(group, project) + "-actions";
    }

    public String getProjectActionId(String group, String project, String action)
    {
        return action + "-" + getProjectMenuId(group, project);
    }

    public void clickProjectAction(String group, String project, String action)
    {
        browser.click(By.id(getProjectMenuId(group, project) + "-link"));
        browser.waitAndClick(By.id(getProjectActionId(group, project, action)));
    }

    public String getGroupActionId(String group, String action)
    {
        return action + "." + getGroupId(group);
    }

    public boolean isGroupActionPresent(String group, String action)
    {
        return browser.isElementIdPresent(getGroupActionId(group, action));
    }

    public void clickGroupAction(String group, String action)
    {
        browser.click(By.id(getGroupActionId(group, action)));
    }

    public String getGroupId(String group)
    {
        if(group == null)
        {
            return "ungroup";
        }
        else
        {
            return "group." + group;
        }
    }

    public String getProjectRowId(String group, String name)
    {
        String prefix;
        if(group == null)
        {
            prefix = "ungrouped";
        }
        else
        {
            prefix = "grouped." + group;
        }

        return prefix + "." + name;
    }

    public String getBuildingSummary(String group, String templateName)
    {
        String id = getProjectRowId(group, templateName) + ".building";
        browser.waitForElement(id);
        return browser.getText(By.id(id));
    }
    
    public List<Long> getBuildIds(String group, String projectName)
    {
        List<Long> result = new LinkedList<Long>();
        int buildIndex = 0;
        String rowId = getBuildLinkId(group, projectName, buildIndex);
        while (browser.isElementIdPresent(rowId))
        {
            result.add(Long.parseLong(browser.getText(By.id(rowId)).split("\\s+")[1]));
            buildIndex++;
            rowId = getBuildLinkId(group, projectName, buildIndex);
        }
        
        return result;
    }

    private String getBuildLinkId(String group, String projectName, int buildIndex)
    {
        return "b" + (buildIndex + 1) + "." + getProjectRowId(group, projectName) + "-link";
    }

    public boolean isResponsibilityPresent(String group, String project)
    {
        return browser.isElementIdPresent(getProjectRowId(group, project) + "_fixing");
    }
}
