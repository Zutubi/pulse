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

package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.admin.CompositePage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectConfigPage;
import com.zutubi.pulse.acceptance.pages.browse.ProjectHomePage;
import static com.zutubi.pulse.master.model.ProjectManager.GLOBAL_PROJECT_NAME;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;

/**
 * Acceptance tests for links that may be added to configuration pages.
 */
public class ConfigLinksAcceptanceTest extends AcceptanceTestBase
{
    public void testLinks() throws Exception
    {
        rpcClient.loginAsAdmin();
        try
        {
            rpcClient.RemoteApi.insertSimpleProject(random, false);
        }
        finally
        {
            rpcClient.logout();
        }

        getBrowser().loginAsAdmin();
        ProjectConfigPage projectConfigPage = getBrowser().openAndWaitFor(ProjectConfigPage.class, random, false);
        assertTrue(projectConfigPage.isLinksBoxPresent());
        assertTrue(projectConfigPage.isLinkPresent("home"));
        assertTrue(projectConfigPage.isLinkPresent("reports"));
        assertTrue(projectConfigPage.isLinkPresent("history"));
        assertTrue(projectConfigPage.isLinkPresent("log"));

        projectConfigPage.clickLink("home");
        ProjectHomePage homePage = getBrowser().createPage(ProjectHomePage.class, random);
        homePage.waitFor();
    }

    public void testLinksNotShownForTemplate() throws Exception
    {
        getBrowser().loginAsAdmin();
        ProjectConfigPage projectConfigPage = getBrowser().openAndWaitFor(ProjectConfigPage.class, GLOBAL_PROJECT_NAME, true);
        assertFalse(projectConfigPage.isLinksBoxPresent());
        assertFalse(projectConfigPage.isLinkPresent("home"));
    }

    public void testLinksNotShownForTypeWithNoLinks() throws Exception
    {
        getBrowser().loginAsAdmin();
        CompositePage compositePage = getBrowser().openAndWaitFor(CompositePage.class, GlobalConfiguration.SCOPE_NAME);
        assertFalse(compositePage.isLinksBoxPresent());
    }
}