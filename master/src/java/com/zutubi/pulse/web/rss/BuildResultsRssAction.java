/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.rss;

import com.sun.syndication.feed.module.content.ContentModule;
import com.sun.syndication.feed.module.content.ContentModuleImpl;
import com.sun.syndication.feed.synd.*;
import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.renderer.BuildResultRenderer;
import com.zutubi.pulse.search.Queries;
import com.zutubi.pulse.search.SearchQuery;
import com.zutubi.pulse.search.BuildResultExpressions;
import com.zutubi.pulse.web.project.ProjectActionSupport;
import org.hibernate.criterion.Order;

import java.io.StringWriter;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * <class-comment/>
 */
public class BuildResultsRssAction extends ProjectActionSupport
{
    private BuildResultRenderer buildResultRenderer;
    private ConfigurationManager configurationManager;

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
            generatePulseBuildResultsFeed();
        }
        else
        {
            generateProjectBuildResultsFeed(project);
        }

        // return the requested feed type. at the moment,
        // we only support RSS.
        return "rss";
    }

    private void generateProjectBuildResultsFeed(Project project)
    {
        // define the query to match the results returned by project build result rss feed.
        SearchQuery<BuildResult> query = queries.getBuildResults();
        query.add(BuildResultExpressions.projectEq(project));
        query.setFirstResult(0);
        query.setMaxResults(10);
        query.add(Order.desc("id"));

        // build the rss feed.
        feed = new SyndFeedImpl();

        // set Title, Description and Link
        feed.setTitle("Pulse build results for " + project.getName());
        feed.setDescription("This feed contains the latest pulse build results for the " + project.getName() + " project.");
        feed.setLink("http://" + configurationManager.getAppConfig().getHostName() +"/currentBuild.action?id=" + project.getId());

        List<SyndEntry> entries = new LinkedList<SyndEntry>();
        for (BuildResult result : query.list())
        {
            SyndEntry entry = new SyndEntryImpl();

            // with rss 2.0, the content is added in the description field.
            SyndContent description = new SyndContentImpl();

            StringBuffer titleBuffer = new StringBuffer();
            titleBuffer.append("Build ");
            titleBuffer.append(result.getNumber());
            titleBuffer.append(" ");
            titleBuffer.append(result.succeeded() ? "succeeded" : "failed");

            // type should be based on user selected type.
            description.setType("text/plain");
            description.setValue(titleBuffer.toString());
            entry.setDescription(description);

            entry.setTitle(titleBuffer.toString());

            ContentModule content = new ContentModuleImpl();
            content.setEncodeds(asList(renderResult(result)));
            entry.setModules(asList(content));

            // NOTES:
            // calling setLink is effectively setting guid without a isPermaLink reference.
            // calling setUri() is equivalent to guid isPermaLink=false - refer to ConverterForRSS094.java
            String permalink = "http://" + configurationManager.getAppConfig().getHostName() +"/viewBuild.action?id=" + result.getId();
            entry.setLink(permalink);
            entry.setPublishedDate(new Date(result.getStamps().getEndTime()));
            entries.add(entry);
        }
        feed.setEntries(entries);


    }

    private void generatePulseBuildResultsFeed()
    {
        // generate the search query.
        SearchQuery<BuildResult> query = queries.getBuildResults();
        query.setFirstResult(0);
        query.setMaxResults(10);
        query.add(Order.desc("id"));

        // build the rss feed.
        feed = new SyndFeedImpl();

        // set Title, Description and Link
        feed.setTitle("Pulse build results");
        feed.setDescription("This feed contains the latest pulse build results.");
        String hostName = configurationManager.getAppConfig().getHostName();
        feed.setLink("http://" + hostName +"/viewProjects.action");

        List<SyndEntry> entries = new LinkedList<SyndEntry>();
        for (BuildResult result : query.list())
        {
            SyndEntry entry = new SyndEntryImpl();

            // with rss 2.0, the content is added in the description field.
            SyndContent description = new SyndContentImpl();

            StringBuffer titleBuffer = new StringBuffer();
            titleBuffer.append("Project ");
            titleBuffer.append(result.getProject().getName());
            titleBuffer.append(" build ");
            titleBuffer.append(result.getNumber());
            titleBuffer.append(" ");
            titleBuffer.append(result.succeeded() ? "succeeded" : "failed");

            // type should be based on user selected type.
            description.setType("text/plain");
            description.setValue(titleBuffer.toString());
            entry.setDescription(description);

            entry.setTitle(titleBuffer.toString());

            ContentModule content = new ContentModuleImpl();
            content.setEncodeds(asList(renderResult(result)));
            entry.setModules(asList(content));

            // NOTES:
            // calling setLink is effectively setting guid without a isPermaLink reference.
            // calling setUri() is equivalent to guid isPermaLink=false - refer to ConverterForRSS094.java
            String permalink = "http://" + hostName +"/viewBuild.action?id=" + result.getId();
            entry.setLink(permalink);
            entry.setPublishedDate(new Date(result.getStamps().getEndTime()));
            entries.add(entry);
        }
        feed.setEntries(entries);
    }

    private <X> List<X> asList(X... objs)
    {
        List<X> l = new LinkedList<X>();
        for (X x : objs)
        {
            l.add(x);
        }
        return l;
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

    public void setQueries(Queries queries)
    {
        this.queries = queries;
    }
}

