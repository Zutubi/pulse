package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.browse.ProjectLogPage;

public class ProjectLogAcceptanceTest extends AcceptanceTestBase
{
    public void testProjectLogAvailable() throws Exception
    {
        rpcClient.loginAsAdmin();
        rpcClient.RemoteApi.insertSimpleProject(random);

        getBrowser().loginAsAdmin();

        ProjectLogPage page = getBrowser().openAndWaitFor(ProjectLogPage.class, random);
        assertTrue(page.isDownloadLinkAvailable());
        assertTrue(page.logContains("Project initialisation succeeded"));
    }

    public void testProjectLogContent() throws Exception
    {
        rpcClient.loginAsAdmin();
        rpcClient.RemoteApi.insertSimpleProject(random);

        getBrowser().loginAsAdmin();

        ProjectLogPage page = getBrowser().openAndWaitFor(ProjectLogPage.class, random);

        assertTrue(page.isDownloadLinkAvailable());

        if (getBrowser().isFirefox())
        {
            page.clickDownloadLink();
            assertTrue(getBrowser().getBodyText().contains("Project initialisation succeeded"));
        }
    }
}
