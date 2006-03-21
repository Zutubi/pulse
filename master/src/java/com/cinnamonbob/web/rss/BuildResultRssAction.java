package com.cinnamonbob.web.rss;

import com.cinnamonbob.model.HistoryPage;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.BuildResult;
import com.cinnamonbob.web.project.ProjectActionSupport;
import com.sun.syndication.feed.synd.*;
import com.opensymphony.util.TextUtils;

import java.util.List;
import java.util.LinkedList;

/**
 * <class-comment/>
 */
public class BuildResultRssAction extends ProjectActionSupport
{
    private static final int NOT_SPECIFIED = -1;

    private String projectName;

    private long projectId = NOT_SPECIFIED;

    private SyndFeed feed;

    public long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public String getProjectName()
    {
        return projectName;
    }

    public void setProjectName(String projectName)
    {
        this.projectName = projectName;
    }

    public SyndFeed getFeed()
    {
        return feed;
    }

    public Project getProject()
    {
        if (projectId != NOT_SPECIFIED)
        {
            return getProjectManager().getProject(projectId);
        }
        else if (TextUtils.stringSet(projectName))
        {
            return getProjectManager().getProject(projectName);
        }
        return null;
    }

    public void addUnknownProjectError()
    {
        if (projectId != NOT_SPECIFIED)
        {
            addActionError("Unknown project [" + projectId + "]");
        }
        else if (TextUtils.stringSet(projectName))
        {
            addActionError("Unknown project [" + projectName + "]");
        }
        else
        {
            addActionError("Require either a project name or id.");
        }
    }

    public String execute()
    {
        Project project = getProject();
        if (project == null)
        {
            addUnknownProjectError();
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
