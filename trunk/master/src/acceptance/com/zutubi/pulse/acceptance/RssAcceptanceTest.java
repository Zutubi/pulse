package com.zutubi.pulse.acceptance;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import com.zutubi.pulse.acceptance.forms.GeneralConfigurationForm;
import com.zutubi.pulse.util.RandomUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * <class-comment/>
 */
public class RssAcceptanceTest extends BaseAcceptanceTestCase
{
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

        loginAsAdmin();

        // ensure that the rss feed is enabled.
        enableRss();
    }

    protected void tearDown() throws Exception
    {

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
        clickLink(Navigation.TAB_PROJECTS);
        assertAndClick(Navigation.Projects.LINK_ADD_PROJECT);

        String projectName = "project " + RandomUtils.randomString(5);
        submitProjectBasicsForm(projectName, "test project description", "http://test.project.com", "cvs", "versioned");
        submitCvsSetupForm(":ext:cvstester@cinnamonbob.com:/cvsroot", "project", "cvs", "");
        submitVersionedSetupForm("pulse.xml");
        assertTablePresent("project.basics");
        return projectName;
    }

    public void testProjectRssFeedGenerationSuccessful() throws IOException, FeedException
    {
        String projectName = createTestProject();

        // directly request the build feed for this project.
        beginAt("rss.action?projectName=" + projectName);

        SyndFeed feed = readResponseAsFeed();
        assertNotNull(feed);
    }

    public void testAllProjectRssFeedGenerationSuccessful() throws FeedException, IOException
    {
        // directly request the build feed for this project.
        beginAt("rss.action");

        SyndFeed feed = readResponseAsFeed();
        assertNotNull(feed);
    }

    private void enableRss()
    {
        beginAt("/");
        clickLinkWithText("Administration");
        clickLink("general.edit");

        GeneralConfigurationForm form = new GeneralConfigurationForm(tester);
        form.setCheckboxChecked("rssEnabled", true);
        form.save();
    }

    private SyndFeed readResponseAsFeed() throws FeedException, IOException
    {
        // validate the response using Rome.
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            dumpResponse(new PrintStream(baos));

            SyndFeedInput input = new SyndFeedInput();
            return input.build(new XmlReader(new ByteArrayInputStream(baos.toByteArray())));
        }
        finally
        {
            dumpResponse(System.out);
        }
    }
}
