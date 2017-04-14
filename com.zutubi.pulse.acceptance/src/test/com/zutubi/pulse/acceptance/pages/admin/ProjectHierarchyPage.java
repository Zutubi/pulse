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
import com.zutubi.pulse.master.webwork.Urls;

import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.PROJECTS_SCOPE;
import org.openqa.selenium.By;

/**
 * The page shown when looking at a project in the hierarchy view.
 */
public class ProjectHierarchyPage extends HierarchyPage
{
    public ProjectHierarchyPage(SeleniumBrowser browser, Urls urls, String project, boolean template)
    {
        super(browser, urls, PROJECTS_SCOPE, project, template);
    }

    public ProjectConfigPage clickConfigure()
    {
        browser.click(By.id(LINK_CONFIGURE));
        return browser.createPage(ProjectConfigPage.class, baseName, template);
    }
}
