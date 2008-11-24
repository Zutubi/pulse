package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.browse.ProjectLogPage;

public class ProjectLogAcceptanceTest extends SeleniumTestBase
{
    public void testProjectLogAvailable() throws Exception
    {
        addProject(random, true);

        loginAsAdmin();

        ProjectLogPage page = new ProjectLogPage(selenium, urls, random);
        page.goTo();
        assertTrue(page.isDownloadLinkAvailable());
        assertTrue(page.getLog().contains("Project initialisation succeeded"));
    }

    public void testProjectLogContent() throws Exception
    {
        addProject(random, true);

        loginAsAdmin();

        ProjectLogPage page = new ProjectLogPage(selenium, urls, random);
        page.goTo();

        assertTrue(page.isDownloadLinkAvailable());

        if (isBrowserFirefox())
        {
            page.clickDownloadLink();
            String log = wipeTimestamps(selenium.getBodyText());
            assertTrue(log.contains("Project initialisation succeeded"));
        }
    }

    private String wipeTimestamps(String tail)
    {
        return tail.replaceAll("(?m)^.*: ", "");
    }
}
