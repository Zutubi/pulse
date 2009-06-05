package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.admin.CompositePage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectConfigPage;
import com.zutubi.pulse.acceptance.pages.browse.ProjectHomePage;
import static com.zutubi.pulse.master.model.ProjectManager.GLOBAL_PROJECT_NAME;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;

/**
 * Acceptance tests for links that may be added to configuration pages.
 */
public class ConfigLinksAcceptanceTest extends SeleniumTestBase
{
    public void testLinks() throws Exception
    {
        xmlRpcHelper.loginAsAdmin();
        try
        {
            xmlRpcHelper.insertSimpleProject(random, false);
        }
        finally
        {
            xmlRpcHelper.logout();
        }

        loginAsAdmin();
        ProjectConfigPage projectConfigPage = browser.openAndWaitFor(ProjectConfigPage.class, random, false);
        assertTrue(projectConfigPage.isLinksBoxPresent());
        assertTrue(projectConfigPage.isLinkPresent("home"));
        assertTrue(projectConfigPage.isLinkPresent("reports"));
        assertTrue(projectConfigPage.isLinkPresent("history"));
        assertTrue(projectConfigPage.isLinkPresent("log"));

        projectConfigPage.clickLink("home");
        ProjectHomePage homePage = browser.create(ProjectHomePage.class, random);
        homePage.waitFor();
    }

    public void testLinksNotShownForTemplate() throws Exception
    {
        loginAsAdmin();
        ProjectConfigPage projectConfigPage = browser.openAndWaitFor(ProjectConfigPage.class, GLOBAL_PROJECT_NAME, true);
        assertFalse(projectConfigPage.isLinksBoxPresent());
        assertFalse(projectConfigPage.isLinkPresent("home"));
    }

    public void testLinksNotShownForTypeWithNoLinks() throws Exception
    {
        loginAsAdmin();
        CompositePage compositePage = browser.openAndWaitFor(CompositePage.class, GlobalConfiguration.SCOPE_NAME);
        assertFalse(compositePage.isLinksBoxPresent());
    }
}