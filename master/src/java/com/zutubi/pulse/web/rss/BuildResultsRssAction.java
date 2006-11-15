package com.zutubi.pulse.web.rss;

import com.sun.syndication.feed.module.content.ContentModule;
import com.sun.syndication.feed.module.content.ContentModuleImpl;
import com.sun.syndication.feed.synd.*;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.BuildManager;
import com.zutubi.pulse.renderer.BuildResultRenderer;
import com.zutubi.pulse.search.BuildResultExpressions;
import com.zutubi.pulse.search.Queries;
import com.zutubi.pulse.search.SearchQuery;
import com.zutubi.pulse.web.project.ProjectActionSupport;
import com.zutubi.pulse.xwork.results.JITFeed;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;

import java.io.StringWriter;
import java.util.*;

/**
 * <class-comment/>
 */
public class BuildResultsRssAction extends ProjectActionSupport
{
    private static final Map<Long, LinkedList<CacheEntry>> cache = new HashMap<Long, LinkedList<CacheEntry>>();

    private BuildResultRenderer buildResultRenderer;
    private MasterConfigurationManager configurationManager;

    private Queries queries;

    private BuildJITFeed feed;

    public JITFeed getFeed()
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

    private void generateProjectBuildResultsFeed(final Project project)
    {
        // define the query to match the results returned by project build result rss feed.
        SearchQuery<Long> query = queries.getIds(BuildResult.class);
        query.add(BuildResultExpressions.projectEq(project));
        query.add(BuildResultExpressions.buildResultCompleted());
        query.setProjection(Projections.id());
        query.setFirstResult(0);
        query.setMaxResults(10);
        query.add(Order.desc("number"));

        feed = new BuildJITFeed(query.list())
        {
            public SyndFeed createFeed()
            {
                // build the rss feed.
                SyndFeed feed = new SyndFeedImpl();

                // set Title, Description and Link
                feed.setTitle("Pulse build results for " + project.getName());
                feed.setDescription("This feed contains the latest pulse build results for the " + project.getName() + " project.");
                feed.setLink(configurationManager.getAppConfig().getBaseUrl() +"/currentBuild.action?id=" + project.getId());

                List<SyndEntry> entries = fetch(project.getId(), results);
                feed.setEntries(entries);
                return feed;
            }
        };
        feed.setBuildManager(buildManager);
    }

    private SyndEntry createBuildResultFeedEntry(BuildResult result)
    {
        SyndEntry entry = new SyndEntryImpl();

        // with rss 2.0, the content is added in the description field.
        SyndContent description = new SyndContentImpl();

        String titleStr = String.format("Build %s %s",
                result.getNumber(),
                (result.succeeded() ? "succeeded" : "failed")
        );

        // type should be based on user selected type.
        description.setType("text/plain");
        description.setValue(titleStr);
        entry.setDescription(description);

        entry.setTitle(titleStr);

        ContentModule content = new ContentModuleImpl();
        content.setEncodeds(asList(renderResult(result)));
        entry.setModules(asList(content));

        // NOTES:
        // calling setLink is effectively setting guid without a isPermaLink reference.
        // calling setUri() is equivalent to guid isPermaLink=false - refer to ConverterForRSS094.java
        String permalink = configurationManager.getAppConfig().getBaseUrl() +"/viewBuild.action?id=" + result.getId();
        entry.setLink(permalink);
        entry.setPublishedDate(new Date(result.getStamps().getEndTime()));
        return entry;
    }

    private void generatePulseBuildResultsFeed()
    {
        // generate the search query.
        SearchQuery<Long> query = queries.getIds(BuildResult.class);
        query.add(BuildResultExpressions.buildResultCompleted());
        query.setProjection(Projections.id());
        query.setFirstResult(0);
        query.setMaxResults(10);
        query.add(BuildResultExpressions.orderByDescEndDate());

        feed = new BuildJITFeed(query.list())
        {
            public SyndFeed createFeed()
            {
                // build the rss feed.
                SyndFeed feed = new SyndFeedImpl();

                // set Title, Description and Link
                feed.setTitle("Pulse build results");
                feed.setDescription("This feed contains the latest pulse build results.");
                feed.setLink(configurationManager.getAppConfig().getBaseUrl() +"/viewProjects.action");

                List<SyndEntry> entries = fetch(-1, results);
                feed.setEntries(entries);
                return feed;
            }
        };
        feed.setBuildManager(buildManager);
    }

    private SyndEntry createProjectBuildResultFeedEntry(BuildResult result)
    {
        SyndEntry entry = new SyndEntryImpl();

        // with rss 2.0, the content is added in the description field.
        SyndContent description = new SyndContentImpl();

        String titleStr = String.format("Project %s build %s %s",
                result.getProject().getName(),
                result.getNumber(),
                (result.succeeded() ? "succeeded" : "failed")
        );

        // type should be based on user selected type.
        description.setType("text/plain");
        description.setValue(titleStr);
        entry.setDescription(description);

        entry.setTitle(titleStr);

        ContentModule content = new ContentModuleImpl();
        content.setEncodeds(asList(renderResult(result)));
        entry.setModules(asList(content));

        // NOTES:
        // calling setLink is effectively setting guid without a isPermaLink reference.
        // calling setUri() is equivalent to guid isPermaLink=false - refer to ConverterForRSS094.java
        String permalink = configurationManager.getAppConfig().getBaseUrl() +"/viewBuild.action?id=" + result.getId();
        entry.setLink(permalink);
        entry.setPublishedDate(new Date(result.getStamps().getEndTime()));
        return entry;
    }

    // DO NOT DELETE THIS UNLESS YOU WANT AN UnsupportedOperationException.
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

    private abstract class BuildJITFeed implements JITFeed
    {
        protected List<Long> results = null;

        private BuildManager buildManager;

        public BuildJITFeed(List<Long> results)
        {
            this.results = results;
        }

        public boolean hasEntries()
        {
            return results.size() > 0;
        }

        public Date getPublishedDate()
        {
            BuildResult result = buildManager.getBuildResult(results.get(0));
            return new Date(result.getStamps().getEndTime());
        }

        public Date getUpdatedDate()
        {
            return null;
        }

        public void setBuildManager(BuildManager buildManager)
        {
            this.buildManager = buildManager;
        }
    }

    private List<SyndEntry> fetch(long key, List<Long> ids)
    {
        if (!cache.containsKey(key))
        {
            cache.put(key, new LinkedList<CacheEntry>());
        }
        LinkedList<CacheEntry> entries = cache.get(key);

        boolean requiresSorting = false;
        for (long id : ids)
        {
            // does this exist in the cache?
            boolean found = false;
            for (CacheEntry entry : entries)
            {
                if (entry.id == id)
                {
                    found = true;
                    break;
                }
            }
            if (!found)
            {
                // create
                SyndEntry entry;

                BuildResult result = buildManager.getBuildResult(id);

                if (key > 0)
                {
                    // we have a project.
                    entry = createBuildResultFeedEntry(result);
                }
                else
                {
                    // no project
                    entry = createProjectBuildResultFeedEntry(result);
                }

                // and add.
                CacheEntry cacheEntry = new CacheEntry(result.getId(), entry);
                entries.add(cacheEntry);
                requiresSorting = true;
            }
        }

        if (requiresSorting)
        {
            Collections.sort(entries, new Comparator<CacheEntry>()
            {
                public int compare(CacheEntry o1, CacheEntry o2)
                {
                    if (o2.id > o1.id)
                    {
                        return 1;
                    }
                    if (o2.id == o1.id)
                    {
                        return 0;
                    }
                    return -1;
                }
            });
        }

        // trim.
        while (entries.size() > 10)
        {
            entries.removeLast();
        }

        List<SyndEntry> syndEntries = new LinkedList<SyndEntry>();
        for (CacheEntry entry : entries)
        {
            syndEntries.add(entry.entry);
        }

        return syndEntries;
    }

    private class CacheEntry
    {

        public CacheEntry(long id, SyndEntry entry)
        {
            this.id = id;
            this.entry = entry;
        }

        long id;
        SyndEntry entry;
    }
}

