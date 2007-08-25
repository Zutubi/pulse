package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.browse.ProjectsPage;
import com.zutubi.pulse.acceptance.pages.browse.ProjectHomePage;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * An acceptance test that adds a very simple project and runs a build as a
 * sanity test.
 */
public class SanityBuildAcceptanceTest extends SeleniumTestBase
{
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    public void testSimpleBuild() throws InterruptedException, IOException, SAXException
    {
        loginAsAdmin();
        goTo(Navigation.LOCATION_PROJECT_CONFIG);
        addProject(random);

        ProjectsPage projectsPage = new ProjectsPage(selenium, urls);
        projectsPage.goTo();
        projectsPage.assertProjectPresent(random);
        projectsPage.triggerProject(random);
        projectsPage.waitFor();

        // Wait for a while so the build can run
        Thread.sleep(30000);

        ProjectHomePage home = new ProjectHomePage(selenium, urls, random);
        home.goTo();
        assertTextPresent("success");
    }

}
