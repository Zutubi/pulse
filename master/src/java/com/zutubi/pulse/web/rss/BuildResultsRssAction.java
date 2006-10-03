package com.zutubi.pulse.web.rss;

import com.sun.syndication.feed.module.content.ContentModule;
import com.sun.syndication.feed.module.content.ContentModuleImpl;
import com.sun.syndication.feed.synd.*;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.renderer.BuildResultRenderer;
import com.zutubi.pulse.search.BuildResultExpressions;
import com.zutubi.pulse.search.Queries;
import com.zutubi.pulse.search.SearchQuery;
import com.zutubi.pulse.web.project.ProjectActionSupport;
import org.hibernate.criterion.Order;

import java.io.StringWriter;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Arrays;

/**
 * <class-comment/>
 */
public class BuildResultsRssAction extends ProjectActionSupport
{
    private BuildResultRenderer buildResultRenderer;
    private MasterConfigurationManager configurationManager;

    private Queries queries;

    private SyndFeed feed;

    public SyndFeed getFeed()
    {
        return feed;
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
            feed = generateFeed(new AllProjectsResultTemplate());
        }
        else
        {
            feed = generateFeed(new ProjectResultTemplate(project));
        }

        // return the requested feed type. at the moment,
        // we only support RSS.
        return "rss";
    }

    private SyndFeedImpl generateFeed(RssFeedTemplate template)
    {
        // build the rss feed.
        SyndFeedImpl feed = new SyndFeedImpl();

        // set Title, Description and Link
        feed.setTitle(template.getTitle());
        feed.setDescription(template.getDescription());
        feed.setLink(template.getLink());

        List<SyndEntry> entries = new LinkedList<SyndEntry>();
        for (BuildResult result : template.getQuery().list())
        {
            SyndEntry entry = new SyndEntryImpl();

            // with rss 2.0, the content is added in the description field.
            SyndContent description = new SyndContentImpl();

            // type should be based on user selected type.
            description.setType("text/plain");
            description.setValue(template.getEntryTitle(result));
            entry.setDescription(description);
            entry.setTitle(template.getTitle());

            ContentModule content = new ContentModuleImpl();
            content.setEncodeds(Arrays.asList(renderResult(result)));
            entry.setModules(Arrays.asList(content));

            // NOTES:
            // calling setLink is effectively setting guid without a isPermaLink reference.
            // calling setUri() is equivalent to guid isPermaLink=false - refer to ConverterForRSS094.java
            entry.setLink(template.getEntryLink(result));
            entry.setPublishedDate(new Date(result.getStamps().getEndTime()));
            entries.add(entry);
        }
        feed.setEntries(entries);

        return feed;
    }

    private String renderResult(BuildResult result)
    {
        StringWriter w = new StringWriter();
        buildResultRenderer.render(configurationManager.getAppConfig().getBaseUrl(),
                result,
                getBuildManager().getChangesForBuild(result),
                BuildResultRenderer.TYPE_HTML , w);
        return w.toString();
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setBuildResultRenderer(BuildResultRenderer buildResultRenderer)
    {
        this.buildResultRenderer = buildResultRenderer;
    }

    public void setQueries(Queries queries)
    {
        this.queries = queries;
    }

    private interface RssFeedTemplate
    {
        SearchQuery<BuildResult> getQuery();
        String getTitle();
        String getDescription();
        String getLink();
        String getEntryTitle(BuildResult result);
        String getEntryLink(BuildResult result);
    }

    private class AllProjectsResultTemplate implements RssFeedTemplate
    {
        public SearchQuery<BuildResult> getQuery()
        {
            SearchQuery<BuildResult> query = queries.getBuildResults();
            query.add(BuildResultExpressions.buildResultCompleted());
            query.setFirstResult(0);
            query.setMaxResults(10);
            query.add(Order.desc("number"));
            return query;
        }

        public String getTitle()
        {
            return "Pulse build results";
        }

        public String getDescription()
        {
            return "This feed contains the latest pulse build results.";
        }

        public String getLink()
        {
            return configurationManager.getAppConfig().getBaseUrl() + "/viewProjects.action";
        }

        public String getEntryTitle(BuildResult result)
        {
            StringBuffer titleBuffer = new StringBuffer();
            titleBuffer.append("Project ");
            titleBuffer.append(result.getProject().getName());
            titleBuffer.append(" build ");
            titleBuffer.append(result.getNumber());
            titleBuffer.append(" ");
            titleBuffer.append(result.succeeded() ? "succeeded" : "failed");
            return titleBuffer.toString();
        }

        public String getEntryLink(BuildResult result)
        {
            return configurationManager.getAppConfig().getBaseUrl() + "/viewBuild.action?id=" + result.getId();
        }
    }

    private class ProjectResultTemplate implements RssFeedTemplate
    {
        private Project project;


        public ProjectResultTemplate(Project project)
        {
            this.project = project;
        }

        public SearchQuery<BuildResult> getQuery()
        {
            SearchQuery<BuildResult> query = queries.getBuildResults();
            query.add(BuildResultExpressions.projectEq(project));
            query.add(BuildResultExpressions.buildResultCompleted());
            query.setFirstResult(0);
            query.setMaxResults(10);
            query.add(Order.desc("number"));
            return query;
        }

        public String getTitle()
        {
            return "Pulse build results for " + project.getName();
        }

        public String getDescription()
        {
            return "This feed contains the latest pulse build results for the " + project.getName() + " project.";
        }

        public String getLink()
        {
            return configurationManager.getAppConfig().getBaseUrl() +"/currentBuild.action?id=" + project.getId();
        }

        public String getEntryTitle(BuildResult result)
        {
            StringBuffer titleBuffer = new StringBuffer();
            titleBuffer.append("Build ");
            titleBuffer.append(result.getNumber());
            titleBuffer.append(" ");
            titleBuffer.append(result.succeeded() ? "succeeded" : "failed");
            return titleBuffer.toString();
        }

        public String getEntryLink(BuildResult result)
        {
            return configurationManager.getAppConfig().getBaseUrl() +"/viewBuild.action?id=" + result.getId();
        }
    }

    private class UserDashboardTemplate implements RssFeedTemplate
    {
        public SearchQuery<BuildResult> getQuery()
        {
            return null;
        }

        public String getTitle()
        {
            return null;
        }

        public String getDescription()
        {
            return null;
        }

        public String getLink()
        {
            return null;
        }

        public String getEntryTitle(BuildResult result)
        {
            return null;
        }

        public String getEntryLink(BuildResult result)
        {
            return null;
        }
    }
}

