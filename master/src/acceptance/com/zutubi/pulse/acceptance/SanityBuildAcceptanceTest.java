package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.browse.ProjectsPage;
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

        ProjectsPage projectsPage = new ProjectsPage(selenium);
        projectsPage.goTo();
        projectsPage.assertProjectPresent(random);
        projectsPage.triggerProject(random);
        projectsPage.waitFor();
    }

}
