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
import com.zutubi.pulse.acceptance.forms.admin.BuildOptionsForm;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.WebUtils;
import org.openqa.selenium.By;

/**
 * The page shown when looking at a project in the configuration view.
 */
public class ProjectConfigPage extends CompositePage
{
    public static final String BUILD_STAGES_BASE    = "stages";
    public static final String BUILD_STAGES_DISPLAY = "build stages";

    private String project;
    private boolean template;

    public ProjectConfigPage(SeleniumBrowser browser, Urls urls, String project, boolean template)
    {
        super(browser, urls, getPath(project));
        this.project = project;
        this.template = template;
    }

    private static String getPath(String project)
    {
        return "projects/" + project;
    }

    public String getPath()
    {
        return getPath(project);
    }

    public String getUrl()
    {
        return urls.adminProject(WebUtils.uriComponentEncode(project));
    }

    public ProjectHierarchyPage clickHierarchy()
    {
        browser.click(By.xpath(getHierarchyXPath()));
        return browser.createPage(ProjectHierarchyPage.class, project, template);
    }

    public BuildOptionsForm clickBuildOptionsAndWait()
    {
        clickComposite("options", "build options");

        BuildOptionsForm form = browser.createForm(BuildOptionsForm.class);
        form.waitFor();
        return form;
    }

    public CleanupRulesPage clickCleanupAndWait()
    {
        ListPage listPage = clickCollection("cleanup", "cleanup rules");
        return browser.waitFor(CleanupRulesPage.class, listPage.getPath());
    }

    public ProjectDependenciesPage clickDependenciesAndWait()
    {
        CompositePage compositePage = clickComposite("dependencies", "project dependencies");
        compositePage.expandTreeNode(compositePage.getPath());
        return browser.waitFor(ProjectDependenciesPage.class, compositePage.getPath());
    }
}
