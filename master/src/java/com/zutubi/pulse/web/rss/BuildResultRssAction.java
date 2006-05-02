/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.rss;

import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.HistoryPage;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.renderer.BuildResultRenderer;
import com.zutubi.pulse.web.project.ProjectActionSupport;
import com.opensymphony.util.TextUtils;
import com.sun.syndication.feed.synd.*;

import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Date;

/**
 * <class-comment/>
 */
public class BuildResultRssAction extends ProjectActionSupport
{
    private static final int NOT_SPECIFIED = -1;

    private String projectName;

    private long projectId = NOT_SPECIFIED;

    private BuildResultRenderer buildResultRenderer;
    private ConfigurationManager configurationManager;

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
        // check that rss is enabled.
        if (!configurationManager.getAppConfig().getRssEnabled())
        {
            addActionError("rss feed is disabled");
            return "disabled";
        }

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
        feed.setTitle("Pulse build results for " + project.getName());
        feed.setDescription("This feed contains the latest pulse build results for the " + project.getName() + " project.");
        feed.setLink("http://" + configurationManager.getAppConfig().getHostName() +"/currentBuild.action?id=" + project.getId());

        List<SyndEntry> entries = new LinkedList<SyndEntry>();
        for (BuildResult result : page.getResults())
        {
            SyndEntry entry = new SyndEntryImpl();

            // with rss 2.0, the content is added in the description field.
            SyndContent description = new SyndContentImpl();

            // type should be based on user selected type.
            description.setType("text/html");
            description.setValue(renderResult(result));
            entry.setDescription(description);

            StringBuffer titleBuffer = new StringBuffer();
            titleBuffer.append("Build ");
            titleBuffer.append(result.getNumber());
            titleBuffer.append(" ");
            titleBuffer.append(result.succeeded() ? "succeeded" : "failed");
            entry.setTitle(titleBuffer.toString());

            String permalink = "http://" + configurationManager.getAppConfig().getHostName() +"/viewBuild.action?id=" + result.getId();
            entry.setUri(permalink);
            entry.setLink(permalink);
            entry.setPublishedDate(new Date(result.getStamps().getEndTime()));
            entries.add(entry);
        }

        feed.setEntries(entries);

        // return the requested feed type. at the moment,
        // we only support RSS.
        return "rss";
    }

    private String renderResult(BuildResult result)
    {
        StringWriter w = new StringWriter();
        buildResultRenderer.render(configurationManager.getAppConfig().getHostName(),
                result,
                BuildResultRenderer.TYPE_HTML , w);
        return w.toString();
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setBuildResultRenderer(BuildResultRenderer buildResultRenderer)
    {
        this.buildResultRenderer = buildResultRenderer;
    }
}
