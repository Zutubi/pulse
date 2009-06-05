package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.browse.ProjectLogPage;

public class ProjectLogAcceptanceTest extends SeleniumTestBase
{
    public void testProjectLogAvailable() throws Exception
    {
        xmlRpcHelper.loginAsAdmin();
        addProject(random, true);

        loginAsAdmin();

        ProjectLogPage page = browser.openAndWaitFor(ProjectLogPage.class, random);
        assertTrue(page.isDownloadLinkAvailable());
        assertTrue(page.logContains("Project initialisation succeeded"));
    }

    public void testProjectLogContent() throws Exception
    {
        xmlRpcHelper.loginAsAdmin();
        addProject(random, true);

        loginAsAdmin();

        ProjectLogPage page = browser.openAndWaitFor(ProjectLogPage.class, random);

        assertTrue(page.isDownloadLinkAvailable());

        if (isBrowserFirefox())
        {
            page.clickDownloadLink();
            assertTrue(browser.bodyTextContains("Project initialisation succeeded"));
        }
    }
}
