package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.acceptance.pages.browse.ProjectLogPage;

public class ProjectLogAcceptanceTest extends SeleniumTestBase
{
    public void testProjectLogAvailable() throws Exception
    {
        createProject(random);

        loginAsAdmin();

        ProjectLogPage page = new ProjectLogPage(selenium, urls, random);
        page.goTo();
        assertTrue(page.isDownloadLinkAvailable());
        assertTrue(page.getLog().contains("Project initialisation succeeded"));
    }

    public void testProjectLogContent() throws Exception
    {
        createProject(random);

        loginAsAdmin();

        ProjectLogPage page = new ProjectLogPage(selenium, urls, random);
        page.goTo();

        assertTrue(page.isDownloadLinkAvailable());

/*
        page.clickDownloadLink();
        String log = wipeTimestamps(selenium.getBodyText());
        assertTrue(log.contains("Project initialisation succeeded"));
*/
    }

    private String wipeTimestamps(String tail)
    {
        return tail.replaceAll("(?m)^.*: ", "");
    }

    private void createProject(String projectName) throws Exception
    {
        xmlRpcHelper.loginAsAdmin();
        xmlRpcHelper.insertSimpleProject(projectName, ProjectManager.GLOBAL_PROJECT_NAME, false);
        xmlRpcHelper.waitForProjectToInitialise(projectName);
        xmlRpcHelper.logout();
    }
}
