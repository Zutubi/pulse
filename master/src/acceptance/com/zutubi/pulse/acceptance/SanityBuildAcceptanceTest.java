package com.zutubi.pulse.acceptance;

import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * An acceptance test that adds a very simple project and runs a build as a
 * sanity test.
 */
public class SanityBuildAcceptanceTest extends SeleniumTestBase
{
    public void testSimpleBuild() throws InterruptedException, IOException, SAXException
    {
        loginAsAdmin();
        goTo(Navigation.LOCATION_PROJECT_CONFIG);
        addProject("project " + random);
    }

}
