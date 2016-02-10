package com.zutubi.pulse.master.xwork.actions.rss;

import com.sun.syndication.feed.WireFeed;
import com.sun.syndication.feed.module.content.ContentModule;
import com.sun.syndication.feed.module.content.ContentModuleImpl;
import com.sun.syndication.feed.synd.*;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectGroup;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.model.persistence.hibernate.HibernateBuildResultExpressions;
import com.zutubi.pulse.master.model.persistence.hibernate.HibernateSearchQueries;
import com.zutubi.pulse.master.model.persistence.hibernate.HibernateSearchQuery;
import com.zutubi.pulse.master.notifications.ResultNotifier;
import com.zutubi.pulse.master.notifications.renderer.BuildResultRenderer;
import com.zutubi.pulse.master.notifications.renderer.RenderService;
import com.zutubi.pulse.master.security.SecurityUtils;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.pulse.master.util.cache.Cache;
import com.zutubi.pulse.master.util.cache.CacheManager;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.pulse.master.webwork.dispatcher.JITFeed;
import com.zutubi.pulse.master.xwork.actions.project.ProjectActionSupport;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.util.WebUtils;
import org.hibernate.criterion.Projections;

import java.io.StringWriter;
import java.util.*;

import static com.zutubi.util.StringUtils.stringSet;

/**
 * Generate a build results rss feed.
 */
public class BuildResultsRssAction extends ProjectActionSupport
{
    private static final Messages I18N = Messages.getInstance(BuildResultsRssAction.class);
    
    private Urls urls;
    private CacheManager cacheManager;

    private BuildResultRenderer buildResultRenderer;
    private MasterConfigurationManager configurationManager;
    private ConfigurationProvider configurationProvider;
    private RenderService renderService;

    private HibernateSearchQueries queries;

    private JITFeed feed;

    private long userId = NONE_SPECIFIED;
    private String groupName;

    public void setUserId(long userId)
    {
        this.userId = userId;
    }

    public void setGroupName(String groupName)
    {
        this.groupName = groupName;
    }

    public JITFeed getFeed()
    {
        return feed;
    }

    public String execute()
    {
        urls = new Urls(getBaseUrl());

        SecurityUtils.runAsSystem(new Runnable()
        {
            public void run()
            {
                RssFeedTemplate feedTemplate;

                // check that rss is enabled.
                if (!configurationProvider.get(GlobalConfiguration.class).isRssEnabled())
                {
                    addActionError("rss feed is disabled");
                    feedTemplate = new ErrorResultTemplate(
                            I18N.format("disabled.title"),
                            I18N.format("disabled.description")
                    );
                }
                else if (projectId != NONE_SPECIFIED)
                {
                    Project project = projectManager.getProject(projectId, false);
                    if (project != null)
                    {
                        feedTemplate = new ProjectResultTemplate(project);
                    }
                    else
                    {
                        feedTemplate = new ErrorResultTemplate(
                                I18N.format("unknown.project.title", projectId),
                                I18N.format("unknown.project.description", projectId)
                        );
                    }
                }
                else if (stringSet(projectName))
                {
                    Project project = projectManager.getProject(projectName, false);
                    if (project != null)
                    {
                        feedTemplate = new ProjectResultTemplate(project);
                    }
                    else
                    {
                        feedTemplate = new ErrorResultTemplate(
                                I18N.format("unknown.project.title", projectName),
                                I18N.format("unknown.project.description", projectName)
                        );
                    }
                }
                else if (stringSet(groupName))
                {
                    ProjectGroup group = projectManager.getProjectGroup(groupName);
                    if (group.getProjects().size() != 0)
                    {
                        feedTemplate = new ProjectGroupTemplate(group);
                    }
                    else
                    {
                        feedTemplate = new ErrorResultTemplate(
                                I18N.format("unknown.group.title", groupName),
                                I18N.format("unknown.group.description", groupName)
                        );
                    }
                }
                else if (userId != NONE_SPECIFIED)
                {
                    User user = userManager.getUser(userId);
                    if (user != null)
                    {
                        feedTemplate = new UserDashboardTemplate(user);
                    }
                    else
                    {
                        feedTemplate = new ErrorResultTemplate(
                                I18N.format("unknown.user.title", userId),
                                I18N.format("unknown.user.description", userId)
                        );
                    }
                }
                else
                {
                    feedTemplate = new AllProjectsResultTemplate();
                }

                feed = new BuildJITFeed(feedTemplate);
            }
        });

        return "rss";
    }

    private String renderResult(BuildResult result)
    {
        StringWriter w = new StringWriter();
        Map<String, Object> dataMap = renderService.getDataMap(result, getBaseUrl());
        buildResultRenderer.render(result, dataMap, "html-email", w);
        return w.toString();
    }

    private String getBaseUrl()
    {
        return configurationProvider.get(GlobalConfiguration.class).getBaseUrl();
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setBuildResultRenderer(BuildResultRenderer buildResultRenderer)
    {
        this.buildResultRenderer = buildResultRenderer;
    }

    public void setQueries(HibernateSearchQueries queries)
    {
        this.queries = queries;
    }

    public void setCacheManager(CacheManager cacheManager)
    {
        this.cacheManager = cacheManager;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }

    public String getProjectName(Project project)
    {
        return projectManager.getProjectConfig(project.getId(), true).getName();
    }

    public void setRenderService(RenderService renderService)
    {
        this.renderService = renderService;
    }

    /**
     * Base class for the different build feed types.
     */
    protected abstract class RssFeedTemplate
    {
        /**
         * The query that generates the data set for the feed.  The ids returned by the
         * queries are ids for build results.
         *
         * @return
         */
        protected abstract HibernateSearchQuery<Long> getQuery();

        /**
         * Get the title for this feed.
         *
         * @return  the title string.
         */
        protected abstract String getTitle();

        /**
         * Get a human readable description of this feed.
         *
         * @return the description.
         */
        protected abstract String getDescription();

        /**
         * Get a link to be associated with this feed.  The link should take the
         * user to the data set contained by this feed.
         *
         * Note that this link will be rendered on an external client, so needs to
         * be fully qualified.
         *
         * @return
         */
        protected abstract String getLink();

        /**
         * Get the title for the feed entry that contains the specified build result.
         *
         * @param result    the build result being rendered.
         * @return
         */
        protected abstract String getEntryTitle(BuildResult result);

        /**
         * Get an id that uniquely identifies the data from this feed.  This is used
         * for caching purposes, and must be unique.
         *
         * @return a unique identifier.
         */
        protected abstract String getUID();

        protected String getEntryLink(BuildResult result)
        {
            return urls.build(result);
        }
    }

    /**
     * This template allows error messages to be returned to the client using the
     * feed format so that it can be read by the feed reader.
     */
    private class ErrorResultTemplate extends RssFeedTemplate
    {
        private String title;
        private String description;

        private ErrorResultTemplate(String title, String description)
        {
            this.title = title;
            this.description = description;
        }

        protected HibernateSearchQuery<Long> getQuery()
        {
            return null;
        }

        protected String getTitle()
        {
            return title;
        }

        protected String getDescription()
        {
            return description;
        }

        protected String getLink()
        {
            return urls.projects();
        }

        protected String getEntryTitle(BuildResult result)
        {
            // never going to happen.
            return null;
        }

        protected String getUID()
        {
            // this query does not return any results, so do not cache it.
            return null;
        }
    }

    private class AllProjectsResultTemplate extends RssFeedTemplate
    {
        public HibernateSearchQuery<Long> getQuery()
        {
            HibernateSearchQuery<Long> query = queries.getIds(BuildResult.class);
            query.add(HibernateBuildResultExpressions.buildResultCompleted());
            query.add(HibernateBuildResultExpressions.isPersonalBuild(false));
            query.setFirstResult(0);
            query.setMaxResults(10);
            query.add(HibernateBuildResultExpressions.orderByDescEndDate());
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
            return urls.projects();
        }

        public String getEntryTitle(BuildResult result)
        {
            return String.format("Project %s build %s %s",
                    getProjectName(result.getProject()),
                    result.getNumber(),
                    (result.healthy() ? "succeeded" : "failed")
            );
        }

        public String getUID()
        {
            return "AllProjectsResultTemplate";
        }
    }

    private class ProjectGroupTemplate extends RssFeedTemplate
    {
        private ProjectGroup group;

        public ProjectGroupTemplate(ProjectGroup group)
        {
            this.group = group;
        }

        public HibernateSearchQuery<Long> getQuery()
        {
            if(group == null)
            {
                return null;
            }

            Collection<Project> projects = group.getProjects();
            if (projects.size() == 0)
            {
                return null;
            }

            HibernateSearchQuery<Long> query = queries.getIds(BuildResult.class);
            query.add(HibernateBuildResultExpressions.projectIn(projects));
            query.add(HibernateBuildResultExpressions.buildResultCompleted());
            query.add(HibernateBuildResultExpressions.isPersonalBuild(false));
            query.setFirstResult(0);
            query.setMaxResults(10);
            query.add(HibernateBuildResultExpressions.orderByDescEndDate());
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
            return urls.projects();
        }

        public String getEntryTitle(BuildResult result)
        {
            return String.format("Project %s build %s %s",
                    getProjectName(result.getProject()),
                    result.getNumber(),
                    (result.healthy() ? "succeeded" : "failed")
            );
        }

        public String getUID()
        {
            return "ProjectGroupTemplate." + ((group != null) ? WebUtils.uriComponentEncode(group.getName()) : "");
        }
    }

    private class ProjectResultTemplate extends RssFeedTemplate
    {
        private Project project;

        public ProjectResultTemplate(Project project)
        {
            this.project = project;
        }

        public HibernateSearchQuery<Long> getQuery()
        {
            HibernateSearchQuery<Long> query = queries.getIds(BuildResult.class);
            query.add(HibernateBuildResultExpressions.projectEq(project));
            query.add(HibernateBuildResultExpressions.buildResultCompleted());
            query.add(HibernateBuildResultExpressions.isPersonalBuild(false));
            query.setFirstResult(0);
            query.setMaxResults(10);
            query.add(HibernateBuildResultExpressions.orderByDescId());
            query.setProjection(Projections.id());
            return query;
        }

        public String getTitle()
        {
            return "Pulse build results for " + getProjectName(project);
        }

        public String getDescription()
        {
            return "This feed contains the latest pulse build results for the " + getProjectName(project) + " project.";
        }

        public String getLink()
        {
            return urls.projectHome(project);
        }

        public String getEntryTitle(BuildResult result)
        {
            return String.format("Build %s %s", result.getNumber(), (result.healthy() ? "succeeded" : "failed"));
        }

        public String getUID()
        {
            return "ProjectResultTemplate." + ((project != null) ? project.getId() : "");
        }
    }

    private class UserDashboardTemplate extends RssFeedTemplate
    {
        private User user;

        public UserDashboardTemplate(User user)
        {
            this.user = user;
        }

        public HibernateSearchQuery<Long> getQuery()
        {
            Set<Project> projects = userManager.getUserProjects(user, projectManager);
            if (projects.size() == 0)
            {
                return null;
            }
            HibernateSearchQuery<Long> query = queries.getIds(BuildResult.class);
            query.add(HibernateBuildResultExpressions.buildResultCompleted());
            query.add(HibernateBuildResultExpressions.projectIn(projects));
            query.add(HibernateBuildResultExpressions.isPersonalBuild(false));
            query.setFirstResult(0);
            query.setMaxResults(10);
            query.add(HibernateBuildResultExpressions.orderByDescEndDate());
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
            return getBaseUrl() + "/dashboard/";
        }

        public String getEntryTitle(BuildResult result)
        {
            return String.format("Project %s build %s %s",
                    getProjectName(result.getProject()),
                    result.getNumber(),
                    (result.healthy() ? "succeeded" : "failed")
            );
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

        public BuildJITFeed(final RssFeedTemplate template)
        {
            SecurityUtils.runAsSystem(new Runnable()
            {
                public void run()
                {
                    final HibernateSearchQuery<Long> query = template.getQuery();
                    if (query != null)
                    {
                        results = query.list();
                    }
                    else
                    {
                        results = new LinkedList<Long>();
                    }
                }
            });
            
            this.template = template;
        }

        public boolean hasEntries()
        {
            return results.size() > 0;
        }

        public Date getPublishedDate()
        {
            final BuildResult result[] = new BuildResult[1];
            SecurityUtils.runAsSystem(new Runnable()
            {
                public void run()
                {
                    result[0] = buildManager.getBuildResult(results.get(0));
                }
            });
            
            return new Date(result[0].getStamps().getEndTime());
        }

        public Date getUpdatedDate()
        {
            return null;
        }

        public WireFeed createWireFeed(String format)
        {
            final SyndFeedImpl feed = new SyndFeedImpl();
            SecurityUtils.runAsSystem(new Runnable()
            {
                public void run()
                {
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

                            // NOTE: We wrap in mutable lists else entry.setPublishedDate will fail.
                            content.setEncodeds(new LinkedList<String>(Arrays.asList(renderResult(result))));
                            entry.setModules(new LinkedList<ContentModule>(Arrays.asList(content)));

                            // NOTES:
                            // calling setLink is effectively setting guid without a isPermaLink reference.
                            // calling setUri() is equivalent to guid isPermaLink=false - refer to ConverterForRSS094.java
                            entry.setLink(template.getEntryLink(result));
                            entry.setPublishedDate(new Date(result.getStamps().getEndTime()));
                            return entry;
                        }
                    });

                    feed.setEntries(entries);
                }
            });
            
            return feed.createWireFeed(format);
        }
    }

    private List<SyndEntry> fetch(String key, List<Long> ids, final SyndFeedEntryFactory factory)
    {
        Cache cache = cacheManager.getCache("BuildResultsRss");
        @SuppressWarnings({"unchecked"})
        LinkedList<CacheEntry> entries = (LinkedList<CacheEntry>) cache.get(key);
        if (entries == null)
        {
            entries = new LinkedList<CacheEntry>();
            if (key != null) // only cache entries where a key is defined.
            {
                cache.put(key, entries);
            }
        }

        synchronized (entries)
        {
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
                    result.loadFeatures(configurationManager.getDataDirectory());
                    result.loadFailedTestResults(configurationManager.getDataDirectory(), ResultNotifier.getFailureLimit());
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

