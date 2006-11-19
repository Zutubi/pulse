package com.zutubi.pulse.web.rss;

import com.sun.syndication.feed.module.content.ContentModule;
import com.sun.syndication.feed.module.content.ContentModuleImpl;
import com.sun.syndication.feed.synd.*;
import com.sun.syndication.feed.WireFeed;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.renderer.BuildResultRenderer;
import com.zutubi.pulse.search.BuildResultExpressions;
import com.zutubi.pulse.search.Queries;
import com.zutubi.pulse.search.SearchQuery;
import com.zutubi.pulse.web.project.ProjectActionSupport;
import com.zutubi.pulse.cache.CacheManager;
import com.zutubi.pulse.cache.Cache;
import com.zutubi.pulse.xwork.results.JITFeed;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;

import java.io.StringWriter;
import java.util.*;

/**
 * <class-comment/>
 */
public class BuildResultsRssAction extends ProjectActionSupport
{
    private CacheManager cacheManager;

    private BuildResultRenderer buildResultRenderer;
    private MasterConfigurationManager configurationManager;

    private Queries queries;

    private JITFeed feed;

    private long userId = -1;
    private long groupId = -1;

    public void setUserId(long userId)
    {
        this.userId = userId;
    }

    public void setGroupId(long groupId)
    {
        this.groupId = groupId;
    }

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
            if (userId != -1)
            {
                User u = userManager.getUser(userId);
                feed = new BuildJITFeed(new UserDashboardTemplate(u));
            }
            else if(groupId != -1)
            {
                ProjectGroup g = projectManager.getProjectGroup(groupId);
                feed = new BuildJITFeed(new ProjectGroupTemplate(g));
            }
            else
            {
                feed = new BuildJITFeed(new AllProjectsResultTemplate());
            }
        }
        else
        {
            feed = new BuildJITFeed(new ProjectResultTemplate(project));
        }

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
        buildResultRenderer.render(configurationManager.getAppConfig().getBaseUrl(),
                result,
                getBuildManager().getChangesForBuild(result),
                "html-email", w);
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

    public void setCacheManager(CacheManager cacheManager)
    {
        this.cacheManager = cacheManager;
    }

    private interface RssFeedTemplate
    {
        SearchQuery<Long> getQuery();

        String getTitle();

        String getDescription();

        String getLink();

        String getEntryTitle(BuildResult result);

        String getEntryLink(BuildResult result);

        String getUID();
    }

    private class AllProjectsResultTemplate implements RssFeedTemplate
    {
        public SearchQuery<Long> getQuery()
        {
            SearchQuery<Long> query = queries.getIds(BuildResult.class);
            query.add(BuildResultExpressions.buildResultCompleted());
            query.setFirstResult(0);
            query.setMaxResults(10);
            query.add(BuildResultExpressions.orderByDescEndDate());
            query.setProjection(Projections.id());
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
            return String.format("Project %s build %s %s",
                    result.getProject().getName(),
                    result.getNumber(),
                    (result.succeeded() ? "succeeded" : "failed")
            );
        }

        public String getEntryLink(BuildResult result)
        {
            return configurationManager.getAppConfig().getBaseUrl() + "/viewBuild.action?id=" + result.getId();
        }

        public String getUID()
        {
            return "AllProjectsResultTemplate";
        }
    }

    private class ProjectGroupTemplate implements RssFeedTemplate
    {
        private ProjectGroup group;

        public ProjectGroupTemplate(ProjectGroup group)
        {
            this.group = group;
        }

        public SearchQuery<Long> getQuery()
        {
            if(group == null)
            {
                return null;
            }

            List<Project> projects = group.getProjects();
            if (projects.size() == 0)
            {
                return null;
            }

            SearchQuery<Long> query = queries.getIds(BuildResult.class);
            query.add(Expression.and(BuildResultExpressions.projectIn(projects), BuildResultExpressions.buildResultCompleted()));
            query.setFirstResult(0);
            query.setMaxResults(10);
            query.add(BuildResultExpressions.orderByDescEndDate());
            query.setProjection(Projections.id());
            return query;
        }

        public String getTitle()
        {
            return "Pulse build results";
        }

        public String getDescription()
        {
            return "This feed contains the latest pulse build results for project group " + group.getName();
        }

        public String getLink()
        {
            return configurationManager.getAppConfig().getBaseUrl() + "/viewProjects.action";
        }

        public String getEntryTitle(BuildResult result)
        {
            return String.format("Project %s build %s %s",
                    result.getProject().getName(),
                    result.getNumber(),
                    (result.succeeded() ? "succeeded" : "failed")
            );
        }

        public String getEntryLink(BuildResult result)
        {
            return configurationManager.getAppConfig().getBaseUrl() + "/viewBuild.action?id=" + result.getId();
        }

        public String getUID()
        {
            return "ProjectGroupTemplate." + ((group != null) ? group.getId() : "");
        }
    }

    private class ProjectResultTemplate implements RssFeedTemplate
    {
        private Project project;

        public ProjectResultTemplate(Project project)
        {
            this.project = project;
        }

        public SearchQuery<Long> getQuery()
        {
            SearchQuery<Long> query = queries.getIds(BuildResult.class);
            query.add(BuildResultExpressions.projectEq(project));
            query.add(BuildResultExpressions.buildResultCompleted());
            query.setFirstResult(0);
            query.setMaxResults(10);
            query.add(Order.desc("stamps.endTime"));
            query.setProjection(Projections.id());
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
            return configurationManager.getAppConfig().getBaseUrl() + "/currentBuild.action?id=" + project.getId();
        }

        public String getEntryTitle(BuildResult result)
        {
            return String.format("Build %s %s", result.getNumber(), (result.succeeded() ? "succeeded" : "failed"));
        }

        public String getEntryLink(BuildResult result)
        {
            return configurationManager.getAppConfig().getBaseUrl() + "/viewBuild.action?id=" + result.getId();
        }

        public String getUID()
        {
            return "ProjectResultTemplate." + ((project != null) ? project.getId() : "");
        }
    }

    private class UserDashboardTemplate implements RssFeedTemplate
    {
        private User user;

        public UserDashboardTemplate(User user)
        {
            this.user = user;
        }

        public SearchQuery<Long> getQuery()
        {
            Set<Project> projects = userManager.getUserProjects(user, projectManager);
            if (projects.size() == 0)
            {
                return null;
            }
            SearchQuery<Long> query = queries.getIds(BuildResult.class);
            query.add(Expression.and(BuildResultExpressions.projectIn(projects), BuildResultExpressions.buildResultCompleted()));
            query.setFirstResult(0);
            query.setMaxResults(10);
            query.add(BuildResultExpressions.orderByDescEndDate());
            query.setProjection(Projections.id());
            return query;
        }

        public String getTitle()
        {
            return "Pulse build results";
        }

        public String getDescription()
        {
            return "This feed contains the latest pulse build results for the dashboard projects.";
        }

        public String getLink()
        {
            return configurationManager.getAppConfig().getBaseUrl() + "/dashboard.action";
        }

        public String getEntryTitle(BuildResult result)
        {
            return String.format("Project %s build %s %s",
                    result.getProject().getName(),
                    result.getNumber(),
                    (result.succeeded() ? "succeeded" : "failed")
            );
        }

        public String getEntryLink(BuildResult result)
        {
            return configurationManager.getAppConfig().getBaseUrl() + "/viewBuild.action?id=" + result.getId();
        }

        public String getUID()
        {
            return "UserDashboardTemplate." + ((user != null) ? user.getId() : "");
        }
    }

    private class BuildJITFeed implements JITFeed
    {
        protected List<Long> results = null;
        protected RssFeedTemplate template;

        public BuildJITFeed(RssFeedTemplate template)
        {
            SearchQuery<Long> query = template.getQuery();
            if (query != null)
            {
                this.results = query.list();
            }
            else
            {
                this.results = new LinkedList<Long>();
            }
            this.template = template;
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

        public WireFeed createWireFeed(String format)
        {
            SyndFeedImpl feed = new SyndFeedImpl();

            // set Title, Description and Link
            feed.setTitle(template.getTitle());
            feed.setDescription(template.getDescription());
            feed.setLink(template.getLink());

            List<SyndEntry> entries = fetch(template.getUID(), results, new SyndFeedEntryFactory()
            {
                public SyndEntry createEntry(BuildResult result)
                {
                    SyndEntry entry = new SyndEntryImpl();

                    // with rss 2.0, the content is added in the description field.
                    SyndContent description = new SyndContentImpl();

                    // type should be based on user selected type.
                    description.setType("text/plain");
                    description.setValue(template.getEntryTitle(result));
                    entry.setDescription(description);
                    entry.setTitle(template.getEntryTitle(result));

                    ContentModule content = new ContentModuleImpl();

                    //NOTE: Do not use Arrays.asList here. The entry.setPublishedDate will fail if you do.
                    content.setEncodeds(asList(renderResult(result)));
                    entry.setModules(asList(content));

                    // NOTES:
                    // calling setLink is effectively setting guid without a isPermaLink reference.
                    // calling setUri() is equivalent to guid isPermaLink=false - refer to ConverterForRSS094.java
                    entry.setLink(template.getEntryLink(result));
                    entry.setPublishedDate(new Date(result.getStamps().getEndTime()));
                    return entry;
                }
            });
            
            feed.setEntries(entries);
            return feed.createWireFeed(format);
        }
    }

    private synchronized List<SyndEntry> fetch(String key, List<Long> ids, SyndFeedEntryFactory factory)
    {
        Cache cache = cacheManager.getCache("BuildResultsRss");
        LinkedList<CacheEntry> entries = (LinkedList<CacheEntry>) cache.get(key);
        if (entries == null)
        {
            entries = new LinkedList<CacheEntry>();
            cache.put(key, entries);
        }

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
                BuildResult result = buildManager.getBuildResult(id);
                SyndEntry entry = factory.createEntry(result);

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

    private interface SyndFeedEntryFactory
    {
        SyndEntry createEntry(BuildResult result);
    }
}

