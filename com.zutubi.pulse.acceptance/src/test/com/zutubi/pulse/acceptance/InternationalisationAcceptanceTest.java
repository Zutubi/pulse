package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.admin.ProjectConfigPage;
import com.zutubi.util.RandomUtils;

public class InternationalisationAcceptanceTest extends SeleniumTestBase
{
    private static final String I18N = "Iñtërnâtiônàlizætiøn";

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        loginAsAdmin();
        xmlRpcHelper.loginAsAdmin();
    }

    public void testI18NCharactersInProjectName() throws Exception
    {
        String name = randomName();
        xmlRpcHelper.insertSimpleProject(name, false);

        ProjectConfigPage projectPage = browser.openAndWaitFor(ProjectConfigPage.class, name, false);
        assertTrue(projectPage.isPresent());
    }

    protected String randomName()
    {
        return I18N + "-" + RandomUtils.randomString(3);
    }
}
