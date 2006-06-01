package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.util.RandomUtils;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import com.sun.syndication.io.FeedException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * <class-comment/>
 */
public class RssAcceptanceTest extends BaseAcceptanceTest
{
    private String projectName;

    public RssAcceptanceTest()
    {
    }

    public RssAcceptanceTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        login("admin", "admin");

        projectName = createTestProject();
    }

    protected void tearDown() throws Exception
    {
        // setup code here..
        projectName = null;

        super.tearDown();
    }

    /**
     * Create a test project and returns its name.
     *
     * @return name identifying the test project.
     */
    protected String createTestProject()
    {
        // navigate to the create project wizard.
        // fill in the form details.
        clickLinkWithText("projects");
        clickLinkWithText("add new project");

        String projectName = "project " + RandomUtils.randomString(5);
        submitProjectBasicsForm(projectName, "test project description", "http://test.project.com", "cvs", "versioned");
        submitCvsSetupForm(":pserver:tester@test.project.com:/cvsroot", "module", "", "");
        submitVersionedSetupForm("pulse.xml");
        assertTablePresent("project.basics");
        return projectName;
    }

    public void testRssFeedGenerationSuccessful() throws IOException, FeedException
    {
        // directly request the build feed for this project.
        beginAt("rss.action?projectName=" + projectName);

        SyndFeed feed = readResponseAsFeed();
        assertNotNull(feed);
    }

    private SyndFeed readResponseAsFeed() throws FeedException, IOException
    {
        // validate the response using Rome.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        dumpResponse(new PrintStream(baos));

        SyndFeedInput input = new SyndFeedInput();
        return input.build(new XmlReader(new ByteArrayInputStream(baos.toByteArray())));
    }

}
