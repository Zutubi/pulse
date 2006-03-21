package com.cinnamonbob.web.rss;

import com.cinnamonbob.model.HistoryPage;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.BuildResult;
import com.cinnamonbob.web.project.ProjectActionSupport;
import com.sun.syndication.feed.synd.*;

import java.util.List;
import java.util.LinkedList;

/**
 * <class-comment/>
 */
public class BuildResultRssAction extends ProjectActionSupport
{
    private long projectId;

    private SyndFeed feed;

    public long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public SyndFeed getFeed()
    {
        return feed;
    }

    public String execute()
    {
        Project project = getProjectManager().getProject(projectId);
        if (project == null)
        {
            addActionError("Unknown project [" + projectId + "]");
            return ERROR;
        }

        HistoryPage page = new HistoryPage(project, 0, 10);

        // Common case
        getBuildManager().fillHistoryPage(page);

        // build the rss feed.
        feed = new SyndFeedImpl();

        // set Title, Description and Link
        feed.setTitle("title");
        feed.setLink("link");
        feed.setUri("uri");
        feed.setDescription("description");

        List<SyndEntry> entries = new LinkedList<SyndEntry>();
        for (BuildResult result : page.getResults())
        {
            SyndEntry entry = new SyndEntryImpl();
            SyndContent description = new SyndContentImpl();
            description.setType("text/html");
            description.setValue("Build " + result.getNumber() + ": " + result.getStateName());

            entry.setDescription(description);
            entries.add(entry);
        }

        feed.setEntries(entries);

        // return the requested feed type. at the moment,
        // we only support RSS.
        return "rss";
    }
}
