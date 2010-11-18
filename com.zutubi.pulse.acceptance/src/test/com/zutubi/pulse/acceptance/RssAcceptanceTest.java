package com.zutubi.pulse.acceptance;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.xwork.actions.rss.BuildResultsRssAction;
import com.zutubi.util.RandomUtils;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class RssAcceptanceTest extends AcceptanceTestBase
{
    private static final Messages I18N = Messages.getInstance(BuildResultsRssAction.class);

    protected void setUp() throws Exception
    {
        super.setUp();

        rpcClient.loginAsAdmin();
    }

    public void testProjectRssFeedGeneration() throws Exception
    {
        String projectName = randomName();

        createProject(projectName, 1);

        SyndFeed feed = readFeed("rss.action?projectName=" + projectName);
        assertThat(feed.getDescription(), containsString(projectName));
        assertEquals(1, feed.getEntries().size());
    }

    public void testUnknownProjectName() throws FeedException, IOException
    {
        String projectName = randomName();

        SyndFeed feed = readFeed("rss.action?projectName=" + projectName);
        assertThat(feed.getTitle(), equalTo(I18N.format("unknown.project.title", projectName)));
        assertThat(feed.getDescription(), equalTo(I18N.format("unknown.project.description", projectName)));
        assertEquals(0, feed.getEntries().size());
    }

    public void testUnknownProjectId() throws FeedException, IOException
    {
        long projectId = RandomUtils.randomLong();

        SyndFeed feed = readFeed("rss.action?projectId=" + projectId);
        assertThat(feed.getTitle(), equalTo(I18N.format("unknown.project.title", projectId)));
        assertThat(feed.getDescription(), equalTo(I18N.format("unknown.project.description", projectId)));
        assertEquals(0, feed.getEntries().size());
    }

    public void testUnknownUser() throws FeedException, IOException
    {
        long userId = RandomUtils.randomLong();

        SyndFeed feed = readFeed("rss.action?userId=" + userId);
        assertThat(feed.getTitle(), equalTo(I18N.format("unknown.user.title", userId)));
        assertThat(feed.getDescription(), equalTo(I18N.format("unknown.user.description", userId)));
        assertEquals(0, feed.getEntries().size());
    }

    public void testUnknownProjectGroup() throws FeedException, IOException
    {
        String groupName = randomName();

        SyndFeed feed = readFeed("rss.action?groupName=" + groupName);
        assertThat(feed.getTitle(), equalTo(I18N.format("unknown.group.title", groupName)));
        assertThat(feed.getDescription(), equalTo(I18N.format("unknown.group.description", groupName)));
        assertEquals(0, feed.getEntries().size());
    }

    public void testAllBuildsRssFeedGeneration() throws Exception
    {
        SyndFeed feed = readFeed("rss.action");
        assertThat(feed.getTitle(), equalTo("Pulse build results"));
        assertThat(feed.getDescription(), equalTo("This feed contains the latest pulse build results."));
    }

    // test authenticated access to the feeds.

    // test content of the feeds - expected builds are returned.

    private void createProject(String projectName, int buildCount) throws Exception
    {
        rpcClient.RemoteApi.insertSimpleProject(projectName, false);
        rpcClient.RemoteApi.waitForProjectToInitialise(projectName);
        for (int i = 1; i <= buildCount; i++)
        {
            rpcClient.RemoteApi.triggerBuild(projectName);
            rpcClient.RemoteApi.waitForBuildToComplete(projectName, i, 5000);
        }
    }

    private SyndFeed readFeed(String path) throws FeedException, IOException
    {
        String content = AcceptanceTestUtils.readUriContent(baseUrl + "/" + path);
        return new SyndFeedInput().build(new XmlReader(new ByteArrayInputStream(content.getBytes())));
    }
}
