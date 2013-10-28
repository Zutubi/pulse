package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.admin.ProjectConfigPage;
import com.zutubi.util.RandomUtils;

public class InternationalisationAcceptanceTest extends AcceptanceTestBase
{
    private static final String I18N = "Iñtërnâtiônàlizætiøn";

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        getBrowser().loginAsAdmin();
        rpcClient.loginAsAdmin();
    }

    @Override
    protected void tearDown() throws Exception
    {
        getBrowser().logout();
        rpcClient.logout();

        super.tearDown();
    }

    public void testI18NCharactersInProjectName() throws Exception
    {
        String name = randomName();
        rpcClient.RemoteApi.insertSimpleProject(name, false);

        ProjectConfigPage projectPage = getBrowser().openAndWaitFor(ProjectConfigPage.class, name, false);
        assertTrue(projectPage.isPresent());
    }

    protected String randomName()
    {
        return I18N + "-" + RandomUtils.insecureRandomString(3);
    }
}
