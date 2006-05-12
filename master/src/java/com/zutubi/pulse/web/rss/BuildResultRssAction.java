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
import com.sun.syndication.feed.synd.*;
import com.sun.syndication.feed.module.content.ContentModule;
import com.sun.syndication.feed.module.content.ContentModuleImpl;

import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Date;

/**
 * <class-comment/>
 */
public class BuildResultRssAction extends ProjectActionSupport
{
    private BuildResultRenderer buildResultRenderer;
    private ConfigurationManager configurationManager;

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
            addUnknownProjectActionError();
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

        // return the requested feed type. at the moment,
        // we only support RSS.
        return "rss";
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
}
