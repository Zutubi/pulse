package com.zutubi.pulse.master.model;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;
import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.resources.api.ResourcePropertyConfiguration;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmCapability;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.master.agent.WorkDirectoryCleanupService;
import com.zutubi.pulse.master.bootstrap.DefaultSetupManager;
import com.zutubi.pulse.master.build.queue.BuildRequestRegistry;
import com.zutubi.pulse.master.events.build.BuildCompletedEvent;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.pulse.master.events.build.PersonalBuildRequestEvent;
import com.zutubi.pulse.master.events.build.SingleBuildRequestEvent;
import com.zutubi.pulse.master.model.persistence.ProjectDao;
import com.zutubi.pulse.master.model.persistence.TestCaseIndexDao;
import com.zutubi.pulse.master.project.ProjectInitialisationService;
import com.zutubi.pulse.master.project.events.ProjectDestructionCompletedEvent;
import com.zutubi.pulse.master.project.events.ProjectInitialisationCompletedEvent;
import com.zutubi.pulse.master.scm.LatestScmRevisionSupplier;
import com.zutubi.pulse.master.scm.ScmClientUtils;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.tove.config.ConfigurationInjector;
import com.zutubi.pulse.master.tove.config.LabelConfiguration;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.group.GroupConfiguration;
import com.zutubi.pulse.master.tove.config.project.*;
import com.zutubi.pulse.master.tove.config.project.reports.*;
import com.zutubi.pulse.master.tove.config.project.triggers.ManualTriggerConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.TriggerConfiguration;
import com.zutubi.tove.config.*;
import com.zutubi.tove.events.ConfigurationEventSystemStartedEvent;
import com.zutubi.tove.events.ConfigurationSystemStartedEvent;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.security.Actor;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.Sort;
import com.zutubi.util.StringUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;
import com.zutubi.util.math.AggregationFunction;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.newArrayList;
import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.EXTENSION_PROJECT_TRIGGERS;

public class DefaultProjectManager implements ProjectManager, ExternalStateManager<ProjectConfiguration>, ConfigurationInjector.ConfigurationSetter<Project>, EventListener
{
    private static final Logger LOG = Logger.getLogger(DefaultProjectManager.class);
    private static final Messages I18N = Messages.getInstance(DefaultProjectManager.class);

    private static final Map<Project.Transition, String> TRANSITION_TO_ACTION_MAP = new HashMap<Project.Transition, String>();

    private ProjectDao projectDao;
    private TestCaseIndexDao testCaseIndexDao;
    private BuildManager buildManager;
    private EventManager eventManager;
    private ChangelistIsolator changelistIsolator;
    private ScmManager scmManager;
    private WorkDirectoryCleanupService workDirectoryCleanupService;

    private ConfigurationProvider configurationProvider;
    private TypeRegistry typeRegistry;
    private ConfigurationTemplateManager configurationTemplateManager;
    private AccessManager accessManager;
    private ProjectInitialisationService projectInitialisationService;
    private BuildRequestRegistry buildRequestRegistry;

    // Protects the five caches defined below.
    private ReadWriteLock cacheLock = new ReentrantReadWriteLock();
    private Map<String, ProjectConfiguration> nameToConfig = new HashMap<String, ProjectConfiguration>();
    /**
     * We use a concurrent map for the configs despite the cache lock, as we
     * need a safe way to do a non-locking get in {@link #setConfiguration}.
     * Compound operations on this map should still acquire the appropriate
     * cache lock (read or write).
     */
    private Map<Long, ProjectConfiguration> idToConfig = new ConcurrentHashMap<Long, ProjectConfiguration>();
    /**
     * We expect the set of invalid projects to be small, in fact usually empty.  So it will rarely be mutated (most
     * removes are ignored as the element is not there) and is fast to copy in any case.
     */
    private Set<Long> invalidProjectHandles = new CopyOnWriteArraySet<Long>();
    private Map<String, Set<ProjectConfiguration>> labelToConfigs = new HashMap<String, Set<ProjectConfiguration>>();
    /**
     * Maps from a project to the handles of those projects that directly
     * depend upon it, as this information is only indirectly available in the
     * configuration.  Handles are used as holding onto instances via
     * references can result in stale data (e.g. CIB-2503).
     */
    private SetMultimap<ProjectConfiguration, Long> configToDownstreamConfigHandles;

    private ConcurrentMap<Long, ReentrantLock> projectStateLocks = new ConcurrentHashMap<Long, ReentrantLock>();

    static
    {
        TRANSITION_TO_ACTION_MAP.put(Project.Transition.DELETE, AccessManager.ACTION_DELETE);
        TRANSITION_TO_ACTION_MAP.put(Project.Transition.PAUSE, ProjectConfigurationActions.ACTION_PAUSE);
        TRANSITION_TO_ACTION_MAP.put(Project.Transition.RESUME, ProjectConfigurationActions.ACTION_PAUSE);
    }

    private void registerConfigListener(final ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
        TypeListener<ProjectConfiguration> projectListener = new TypeAdapter<ProjectConfiguration>(ProjectConfiguration.class)
        {
            public void postInsert(ProjectConfiguration instance)
            {
                // The project instance created in this transaction needs the
                // config wired in manually, as it was not in idToConfig when
                // the project was created.
                projectDao.findById(instance.getProjectId()).setConfig(instance);
                cacheLock.writeLock().lock();
                try
                {
                    registerProjectConfig(instance, true);
                    refreshDownstreamCache();
                }
                finally
                {
                    cacheLock.writeLock().unlock();
                }
            }

            public void postDelete(ProjectConfiguration instance)
            {
                cacheLock.writeLock().lock();
                try
                {
                    nameToConfig.remove(instance.getName());
                    idToConfig.remove(instance.getProjectId());
                    invalidProjectHandles.remove(instance.getHandle());
                    removeFromLabelMap(instance);
                    reloadDownstreamProjects(instance);
                    refreshDownstreamCache();
                }
                finally
                {
                    cacheLock.writeLock().unlock();
                }
            }

            public void postSave(ProjectConfiguration instance, boolean nested)
            {
                ProjectConfiguration old;

                cacheLock.writeLock().lock();
                try
                {
                    // Tricky: the name may have changed.
                    old = idToConfig.remove(instance.getProjectId());
                    if(old != null)
                    {
                        nameToConfig.remove(old.getName());
                        invalidProjectHandles.remove(old.getHandle());
                        removeFromLabelMap(old);
                        reloadDownstreamProjects(old);
                    }

                    registerProjectConfig(instance, false);
                    refreshDownstreamCache();
                }
                finally
                {
                    cacheLock.writeLock().unlock();
                }

                if (old != null)
                {
                    // Check for addition or removal of an SCM.  Note this must
                    // come after registering the new config.
                    checkForScmChanges(instance, old);
                    checkForPersistentWorkDirChange(instance, old);
                    checkForStageRemoval(instance, old);
                }
            }
        };
        projectListener.register(configurationProvider, true);
    }

    private void reloadDownstreamProjects(ProjectConfiguration oldProjectConfiguration)
    {
        // Patch for CIB-2503: manually figure out projects that can "reach" a
        // changed project via dependency configuration, and refresh them in
        // the caches.  Note this is recursive to handle transitive
        // dependencies.
        Set<Long> downstreamHandles = configToDownstreamConfigHandles.get(oldProjectConfiguration);
        if (downstreamHandles != null)
        {
            Iterable<ProjectConfiguration> downstreamConfigs = transform(downstreamHandles, new Function<Long, ProjectConfiguration>()
            {
                public ProjectConfiguration apply(Long handle)
                {
                    return configurationProvider.get(handle, ProjectConfiguration.class);
                }
            });

            for (ProjectConfiguration config: downstreamConfigs)
            {
                if (config != null)
                {
                    ProjectConfiguration cachedConfig = idToConfig.remove(config.getProjectId());
                    if (cachedConfig != null)
                    {
                        invalidProjectHandles.remove(cachedConfig.getHandle());
                        nameToConfig.remove(cachedConfig.getName());
                        removeFromLabelMap(cachedConfig);
                        registerProjectConfig(config, false);
                    }

                    reloadDownstreamProjects(cachedConfig);
                }
            }
        }
    }

    private void checkForScmChanges(ProjectConfiguration instance, ProjectConfiguration old)
    {
        ScmConfiguration oldScm = old.getScm();
        ScmConfiguration newScm = instance.getScm();

        if (newScm == null)
        {
            if (oldScm != null)
            {
                makeStateTransition(instance.getProjectId(), Project.Transition.CLEANUP);
            }
        }
        else if (configurationTemplateManager.isDeeplyValid(instance.getConfigurationPath()))
        {
            if (oldScm == null || !oldScm.getType().equals(newScm.getType()))
            {
                makeStateTransition(instance.getProjectId(), Project.Transition.INITIALISE);
            }
            else if (newScm != oldScm)
            {
                ScmClient client = null;
                try
                {
                    client = scmManager.createClient(instance, newScm);
                    if (client.configChangeRequiresClean(oldScm, newScm))
                    {
                        makeStateTransition(instance.getProjectId(), Project.Transition.INITIALISE);
                    }
                }
                catch (ScmException e)
                {
                    LOG.severe(e);
                }
                finally
                {
                    IOUtils.close(client);
                }
            }
        }
    }

    private void checkForPersistentWorkDirChange(ProjectConfiguration instance, ProjectConfiguration old)
    {
        BootstrapConfiguration newBootstrap = instance.getBootstrap();
        BootstrapConfiguration oldBootstrap = old.getBootstrap();
        if (oldBootstrap == null || newBootstrap == null ||
            !Objects.equal(newBootstrap.getPersistentDirPattern(), oldBootstrap.getPersistentDirPattern()))
        {
            workDirectoryCleanupService.asyncEnqueueCleanupMessages(old, null);
        }
    }

    private void checkForStageRemoval(ProjectConfiguration instance, ProjectConfiguration old)
    {
        for (final BuildStageConfiguration oldStage: old.getStages().values())
        {
            if (!Iterables.any(instance.getStages().values(), new Predicate<BuildStageConfiguration>()
            {
                public boolean apply(BuildStageConfiguration stage)
                {
                    return stage.getHandle() == oldStage.getHandle();
                }
            }))
            {
                workDirectoryCleanupService.asyncEnqueueCleanupMessages(old, oldStage);
            }
        }
    }
    
    private void initialise()
    {
        changelistIsolator = new ChangelistIsolator(buildManager);
        changelistIsolator.setScmManager(scmManager);

        initialiseProjects();

        // create default project if it is required.
        ensureDefaultProjectDefined();

        refreshDownstreamCache();
    }

    /**
     * Ensure that the global template project is defined.
     */
    private void ensureDefaultProjectDefined()
    {
        if (DefaultSetupManager.initialInstallation)
        {
            try
            {
                ProjectConfiguration globalProject = new ProjectConfiguration();
                globalProject.setName(GLOBAL_PROJECT_NAME);
                globalProject.setDescription("The global template is the base of the project template hierarchy.  Configuration shared among all projects should be added here.");
                globalProject.setPermanent(true);

                Map<String, TriggerConfiguration> triggers = new ConfigurationMap<TriggerConfiguration>();
                ManualTriggerConfiguration trigger = new ManualTriggerConfiguration();
                trigger.setName(DEFAULT_TRIGGER_NAME);
                triggers.put(DEFAULT_TRIGGER_NAME, trigger);
                globalProject.getExtensions().put(EXTENSION_PROJECT_TRIGGERS, triggers);

                // All users can view all projects by default.
                GroupConfiguration group = configurationProvider.get(PathUtils.getPath(MasterConfigurationRegistry.GROUPS_SCOPE, UserManager.ALL_USERS_GROUP_NAME), GroupConfiguration.class);
                globalProject.addPermission(new ProjectAclConfiguration(group, AccessManager.ACTION_VIEW));

                // Anonymous users can view all projects by default (but only
                // when anonymous access is explicitly enabled).
                group = configurationProvider.get(PathUtils.getPath(MasterConfigurationRegistry.GROUPS_SCOPE, UserManager.ANONYMOUS_USERS_GROUP_NAME), GroupConfiguration.class);
                globalProject.addPermission(new ProjectAclConfiguration(group, AccessManager.ACTION_VIEW));

                // Project admins can do just that
                group = configurationProvider.get(PathUtils.getPath(MasterConfigurationRegistry.GROUPS_SCOPE, UserManager.PROJECT_ADMINS_GROUP_NAME), GroupConfiguration.class);
                globalProject.addPermission(new ProjectAclConfiguration(group, AccessManager.ACTION_ADMINISTER));

                addDefaultReports(globalProject);

                CompositeType projectType = typeRegistry.getType(ProjectConfiguration.class);
                MutableRecord globalTemplate = projectType.unstantiate(globalProject, PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, GLOBAL_PROJECT_NAME));
                configurationTemplateManager.markAsTemplate(globalTemplate);
                configurationTemplateManager.insertRecord(MasterConfigurationRegistry.PROJECTS_SCOPE, globalTemplate);
            }
            catch (TypeException e)
            {
                LOG.severe("Unable to create global project template: " + e.getMessage(), e);
            }
        }
    }

    private void addDefaultReports(ProjectConfiguration globalProject)
    {
        ReportGroupConfiguration buildTrendsGroup = new ReportGroupConfiguration(I18N.format("report.group.build.trends"));
        buildTrendsGroup.addReport(createBuildResultReport());
        buildTrendsGroup.addReport(createBuildTimeReport());
        buildTrendsGroup.addReport(createErrorCountReport());
        buildTrendsGroup.addReport(createWarningCountReport());
        globalProject.addReportGroup(buildTrendsGroup);

        ReportGroupConfiguration testTrendsGroup = new ReportGroupConfiguration(I18N.format("report.group.test.trends"));
        testTrendsGroup.setDefaultTimeFrame(30);
        testTrendsGroup.setDefaultTimeUnit(ReportTimeUnit.BUILDS);
        testTrendsGroup.addReport(createTestsRunReport());
        testTrendsGroup.addReport(createTestSuccessRateReport());
        testTrendsGroup.addReport(createBuildTestBreakdownReport());
        testTrendsGroup.addReport(createStageTestBreakdownReport());
        globalProject.addReportGroup(testTrendsGroup);
    }

    private ReportConfiguration createBuildResultReport()
    {
        ReportConfiguration reportConfig = new ReportConfiguration(
                I18N.format("report.build.results"),
                ChartType.STACKED_BAR_CHART,
                false,
                DomainUnit.DAYS,
                AggregationFunction.SUM,
                I18N.format("report.build.results.range"));

        reportConfig.addSeries(new BuildReportSeriesConfiguration(
                I18N.format("report.build.results.series.succeeded"),
                BuildMetric.SUCCESS_COUNT,
                false,
                ChartColours.SUCCESS_FILL.toString()
        ));

        reportConfig.addSeries(new BuildReportSeriesConfiguration(
                I18N.format("report.build.results.series.broken"),
                BuildMetric.BROKEN_COUNT,
                false,
                ChartColours.BROKEN_FILL.toString()
        ));
        return reportConfig;
    }

    private ReportConfiguration createBuildTimeReport()
    {
        ReportConfiguration reportConfig = new ReportConfiguration(
                I18N.format("report.build.time"),
                ChartType.LINE_CHART,
                false,
                DomainUnit.DAYS,
                AggregationFunction.MEAN,
                I18N.format("report.build.time.range"));

        reportConfig.addSeries(new BuildReportSeriesConfiguration(
                I18N.format("report.build.time.series.builds"),
                BuildMetric.ELAPSED_TIME,
                true
        ));

        reportConfig.addSeries(new StageReportSeriesConfiguration(
                I18N.format("report.build.time.series.stages"),
                StageMetric.ELAPSED_TIME,
                true,
                AggregationFunction.MEAN
        ));
        return reportConfig;
    }

    private ReportConfiguration createErrorCountReport()
    {
        ReportConfiguration reportConfig = new ReportConfiguration(
                I18N.format("report.error.count"),
                ChartType.LINE_CHART,
                false,
                DomainUnit.DAYS,
                AggregationFunction.MEAN,
                I18N.format("report.error.count.range"));

        reportConfig.addSeries(new BuildReportSeriesConfiguration(
                I18N.format("report.error.count.series"),
                BuildMetric.ERROR_COUNT,
                false,
                ChartColours.ERROR_LINE.toString()
        ));
        return reportConfig;
    }

    private ReportConfiguration createWarningCountReport()
    {
        ReportConfiguration reportConfig = new ReportConfiguration(
                I18N.format("report.warning.count"),
                ChartType.LINE_CHART,
                false,
                DomainUnit.DAYS,
                AggregationFunction.MEAN,
                I18N.format("report.warning.count.range"));

        reportConfig.addSeries(new BuildReportSeriesConfiguration(
                I18N.format("report.warning.count.series"),
                BuildMetric.WARNING_COUNT,
                false,
                ChartColours.WARNING_LINE.toString()
        ));
        return reportConfig;
    }

    private ReportConfiguration createTestsRunReport()
    {
        ReportConfiguration reportConfig = new ReportConfiguration(
                I18N.format("report.tests.run"),
                ChartType.LINE_CHART,
                false,
                DomainUnit.BUILD_IDS,
                null,
                I18N.format("report.tests.run.range"));

        reportConfig.addSeries(new BuildReportSeriesConfiguration(
                I18N.format("report.tests.run.series.builds"),
                BuildMetric.TEST_TOTAL_COUNT,
                true
        ));

        reportConfig.addSeries(new StageReportSeriesConfiguration(
                I18N.format("report.tests.run.series.stages"),
                StageMetric.TEST_TOTAL_COUNT,
                true,
                AggregationFunction.MEAN
        ));
        return reportConfig;
    }

    private ReportConfiguration createTestSuccessRateReport()
    {
        ReportConfiguration reportConfig = new ReportConfiguration(
                I18N.format("report.tests.success"),
                ChartType.STACKED_AREA_CHART,
                false,
                DomainUnit.BUILD_IDS,
                null,
                I18N.format("report.tests.success.range"));

        reportConfig.addSeries(new BuildReportSeriesConfiguration(
                I18N.format("report.tests.success.series.passed"),
                BuildMetric.TEST_SUCCESS_PERCENTAGE,
                false,
                ChartColours.SUCCESS_FILL.toString()
        ));

        reportConfig.addSeries(new BuildReportSeriesConfiguration(
                I18N.format("report.tests.success.series.broken"),
                BuildMetric.TEST_BROKEN_PERCENTAGE,
                false,
                ChartColours.BROKEN_FILL.toString()
        ));
        return reportConfig;
    }

    private ReportConfiguration createBuildTestBreakdownReport()
    {
        ReportConfiguration reportConfig = new ReportConfiguration(
                I18N.format("report.test.breakdown.builds"),
                ChartType.STACKED_BAR_CHART,
                false,
                DomainUnit.BUILD_IDS,
                null,
                I18N.format("report.test.breakdown.builds.range"));

        reportConfig.addSeries(new BuildReportSeriesConfiguration(
                I18N.format("report.test.breakdown.builds.series.pass"),
                BuildMetric.TEST_PASS_COUNT,
                false,
                ChartColours.SUCCESS_FILL.toString()
        ));

        reportConfig.addSeries(new BuildReportSeriesConfiguration(
                I18N.format("report.test.breakdown.builds.series.expected.failure"),
                BuildMetric.TEST_EXPECTED_FAIL_COUNT,
                false,
                ChartColours.EXPECTED_FAIL_FILL.toString()
        ));

        reportConfig.addSeries(new BuildReportSeriesConfiguration(
                I18N.format("report.test.breakdown.builds.series.skipped"),
                BuildMetric.TEST_SKIPPED_COUNT,
                false,
                ChartColours.NOTHING_FILL.toString()
        ));

        reportConfig.addSeries(new BuildReportSeriesConfiguration(
                I18N.format("report.test.breakdown.builds.series.failure"),
                BuildMetric.TEST_FAIL_COUNT,
                false,
                ChartColours.FAIL_FILL.toString()
        ));

        reportConfig.addSeries(new BuildReportSeriesConfiguration(
                I18N.format("report.test.breakdown.builds.series.error"),
                BuildMetric.TEST_ERROR_COUNT,
                false,
                ChartColours.BROKEN_FILL.toString()
        ));

        return reportConfig;
    }

    private ReportConfiguration createStageTestBreakdownReport()
    {
        ReportConfiguration reportConfig = new ReportConfiguration(
                I18N.format("report.test.breakdown.stages"),
                ChartType.STACKED_BAR_CHART,
                false,
                DomainUnit.BUILD_IDS,
                null,
                I18N.format("report.test.breakdown.builds.range"));

        reportConfig.addSeries(new StageReportSeriesConfiguration(
                I18N.format("report.test.breakdown.stages.series.pass"),
                StageMetric.TEST_PASS_COUNT,
                false,
                ChartColours.SUCCESS_FILL.toString(),
                AggregationFunction.MEAN
        ));

        reportConfig.addSeries(new StageReportSeriesConfiguration(
                I18N.format("report.test.breakdown.stages.series.expected.failure"),
                StageMetric.TEST_EXPECTED_FAIL_COUNT,
                false,
                ChartColours.EXPECTED_FAIL_FILL.toString(),
                AggregationFunction.MEAN
        ));

        reportConfig.addSeries(new StageReportSeriesConfiguration(
                I18N.format("report.test.breakdown.stages.series.skipped"),
                StageMetric.TEST_SKIPPED_COUNT,
                false,
                ChartColours.NOTHING_FILL.toString(),
                AggregationFunction.MEAN
        ));

        reportConfig.addSeries(new StageReportSeriesConfiguration(
                I18N.format("report.test.breakdown.stages.series.failure"),
                StageMetric.TEST_FAIL_COUNT,
                false,
                ChartColours.FAIL_FILL.toString(),
                AggregationFunction.MEAN
        ));

        reportConfig.addSeries(new StageReportSeriesConfiguration(
                I18N.format("report.test.breakdown.stages.series.error"),
                StageMetric.TEST_ERROR_COUNT,
                false,
                ChartColours.BROKEN_FILL.toString(),
                AggregationFunction.MEAN
        ));

        return reportConfig;
    }

    public long createState(ProjectConfiguration instance)
    {
        Project project = new Project();
        save(project);

        return project.getId();
    }

    public void rollbackState(long id)
    {
        Project project = projectDao.findById(id);
        if (project != null)
        {
            projectDao.delete(project);
        }
    }

    public Project getState(long id)
    {
        return projectDao.findById(id);
    }

    private ReentrantLock getProjectStateLock(long projectId)
    {
        ReentrantLock lock = projectStateLocks.get(projectId);
        // If a lock already exists it will never change, so this null check is
        // safe despite not being atomic.
        if (lock == null)
        {
            // We may race with another thread to create the lock but only one
            // will win and both will end up with the same lock.  This is why
            // we must lookup the value in the map rather than presume ours was
            // the one that was added.
            projectStateLocks.putIfAbsent(projectId, new ReentrantLock());
            lock = projectStateLocks.get(projectId);
        }

        return lock;
    }

    public void runUnderProjectLocks(Runnable fn, long... projectIds)
    {
        lockProjectStates(projectIds);
        try
        {
            fn.run();
        }
        finally
        {
            unlockProjectStates(projectIds);
        }
    }

    /**
     * Acquires a lock on the states for the given projects.  The lock must be
     * held when performing compound logic involving the project state.  The
     * lock is exclusive and reentrant.  When locking multiple states, pass
     * them all to this method at once to ensure a consistent locking order.
     *
     * @param projectIds identifiers of the projects to lock the state for
     *
     * @see #unlockProjectStates(long...)
     */
    private void lockProjectStates(long... projectIds)
    {
        projectIds = sortForLock(projectIds);
        for (long id: projectIds)
        {
            getProjectStateLock(id).lock();
        }
    }

    /**
     * Releases a lock on the states for the given projects.  The caller must
     * currently hold the locks.  When unlocking multiple states, pass them all
     * to this method at once to ensure a consistent unlocking order.
     *
     * @param projectIds identifiers of the projects to unlock the state for
     *
     * @see #lockProjectStates(long...)
     */
    private void unlockProjectStates(long... projectIds)
    {
        projectIds = sortForLock(projectIds);
        for (int i = projectIds.length - 1; i >= 0; i--)
        {
            getProjectStateLock(projectIds[i]).unlock();
        }
    }

    private long[] sortForLock(long... projectIds)
    {
        long[] idsCopy = new long[projectIds.length];
        System.arraycopy(projectIds, 0, idsCopy, 0, projectIds.length);
        Arrays.sort(idsCopy);
        return idsCopy;
    }

    private void initialiseProjects()
    {
        cacheLock.writeLock().lock();
        try
        {
            for (ProjectConfiguration config: configurationProvider.getAll(ProjectConfiguration.class))
            {
                registerProjectConfig(config, true);
            }
        }
        finally
        {
            cacheLock.writeLock().unlock();
        }

        ProjectConfiguration rootProject = configurationTemplateManager.getRootInstance(MasterConfigurationRegistry.PROJECTS_SCOPE, ProjectConfiguration.class);
        if (rootProject != null)
        {
            // Create dummy configuration for all projects that are missing theirs.
            CompositeType projectType = typeRegistry.getType(ProjectConfiguration.class);
            long rootHandle = rootProject.getHandle();
            for (Project missingConfig: filter(projectDao.findAll(), ProjectPredicates.noConfig()))
            {
                insertDummyConfiguration(missingConfig, projectType, rootHandle);
            }
        }

        // Restore project states that are out of sync due to unclean shutdown.
        for (Project project: filter(projectDao.findAll(), ProjectPredicates.exists()))
        {
            makeStateTransition(project.getId(), Project.Transition.STARTUP);
        }
    }

    private void registerProjectConfig(ProjectConfiguration projectConfig, boolean checkLifecycle)
    {
        nameToConfig.put(projectConfig.getName(), projectConfig);
        idToConfig.put(projectConfig.getProjectId(), projectConfig);
        if (configurationTemplateManager.isDeeplyValid(projectConfig.getConfigurationPath()))
        {
            if (checkLifecycle)
            {
                checkProjectLifecycle(projectConfig);
            }

            for (LabelConfiguration label: projectConfig.getLabels())
            {
                Set<ProjectConfiguration> projects = labelToConfigs.get(label.getLabel());
                if(projects == null)
                {
                    projects = new TreeSet<ProjectConfiguration>(new NamedConfigurationComparator());
                    labelToConfigs.put(label.getLabel(), projects);
                }

                projects.add(projectConfig);
            }
        }
        else
        {
            invalidProjectHandles.add(projectConfig.getHandle());
        }
    }

    private void insertDummyConfiguration(Project project, CompositeType projectType, long rootHandle)
    {
        LOG.warning("Inserting dummy configuration for orphaned project row '" + project.getId() + "'");
        try
        {
            ProjectConfiguration dummy = new ProjectConfiguration("orphaned-database-entry-" + project.getId());
            dummy.setDescription("Automatically-created dummy project for database entry missing corresponding configuration.  " +
                "You should either delete this project, or re-create valid configuration for it.");
            dummy.setProjectId(project.getId());

            MutableRecord record = projectType.unstantiate(dummy, null);
            configurationTemplateManager.setParentTemplate(record, rootHandle);
            configurationTemplateManager.insertRecord(MasterConfigurationRegistry.PROJECTS_SCOPE, record);
        }
        catch (TypeException e)
        {
            LOG.severe(e);
        }
    }

    private void refreshDownstreamCache()
    {
        configToDownstreamConfigHandles = HashMultimap.create();

        for (ProjectConfiguration config: idToConfig.values())
        {
            for (DependencyConfiguration dependency: config.getDependencies().getDependencies())
            {
                configToDownstreamConfigHandles.put(dependency.getProject(), config.getHandle());
            }
        }
    }

    private void checkProjectLifecycle(ProjectConfiguration projectConfig)
    {
        long id = projectConfig.getProjectId();
        Project project = getProject(id, false);
        if (project == null)
        {
            LOG.severe("Unexpected missing project instance for id '" + id + "'");
            return;
        }

        lockProjectStates(id);
        try
        {
            Project.State state = project.getState();
            if (state == Project.State.INITIAL)
            {
                makeStateTransition(project.getId(), Project.Transition.INITIALISE);
            }
            else if (state.isInitialised())
            {
                projectInitialisationService.registerInitialised(projectConfig);
            }
        }
        finally
        {
            unlockProjectStates(id);
        }
    }

    private void removeFromLabelMap(ProjectConfiguration projectConfig)
    {
        for(LabelConfiguration label: projectConfig.getLabels())
        {
            Set<ProjectConfiguration> projects = labelToConfigs.get(label.getLabel());
            if(projects != null)
            {
                projects.remove(projectConfig);
                if(projects.size() == 0)
                {
                    labelToConfigs.remove(label.getLabel());
                }
            }
        }
    }

    public void save(Project project)
    {
        projectDao.save(project);
    }

    public Iterable<ProjectConfiguration> getAllProjectConfigs(boolean allowInvalid)
    {
        cacheLock.readLock().lock();
        try
        {
            Iterable<ProjectConfiguration> result = nameToConfig.values();
            if (!allowInvalid && invalidProjectHandles.size() > 0)
            {
                result = Iterables.filter(result, new Predicate<ProjectConfiguration>()
                {
                    public boolean apply(ProjectConfiguration project)
                    {
                        return !invalidProjectHandles.contains(project.getHandle());
                    }
                });
            }

            return result;
        }
        finally
        {
            cacheLock.readLock().unlock();
        }
    }

    public ProjectConfiguration getProjectConfig(String name, boolean allowInvalid)
    {
        cacheLock.readLock().lock();
        try
        {
            return checkValidity(nameToConfig.get(name), allowInvalid);
        }
        finally
        {
            cacheLock.readLock().unlock();
        }
    }

    public ProjectConfiguration getProjectConfig(long id, boolean allowInvalid)
    {
        cacheLock.readLock().lock();
        try
        {
            ProjectConfiguration configuration = idToConfig.get(id);
            if (configuration != null)
            {
                configuration = configurationProvider.get(configuration.getHandle(), ProjectConfiguration.class);
            }
            return checkValidity(configuration, allowInvalid);
        }
        finally
        {
            cacheLock.readLock().unlock();
        }
    }

    private ProjectConfiguration checkValidity(ProjectConfiguration configuration, boolean allowInvalid)
    {
        if(!allowInvalid && configuration != null && !configurationTemplateManager.isDeeplyValid(configuration.getConfigurationPath()))
        {
            return null;
        }
        return configuration;
    }

    public Project getProject(String name, boolean allowInvalid)
    {
        ProjectConfiguration config = getProjectConfig(name, allowInvalid);
        if (config == null)
        {
            return null;
        }
        return getProject(config.getProjectId(), allowInvalid);
    }

    public Project getProject(long id, boolean allowInvalid)
    {
        Project project = projectDao.findById(id);
        if (isCompleteAndValid(project, allowInvalid))
        {
            return project;
        }

        return null;
    }

    public List<Project> getProjects(Collection<Long> ids, boolean allowInvalid)
    {
        List<Project> result = new LinkedList<Project>();
        for (Long id : ids)
        {
            Project project = projectDao.findById(id);
            if (ProjectPredicates.exists(project))
            {
                result.add(project);
            }
        }
        return filterCompleteAndValidProjects(result, allowInvalid);
    }

    public List<Project> getProjects(boolean allowInvalid)
    {
        List<Project> result = projectDao.findAll();
        return filterCompleteAndValidProjects(result, allowInvalid);
    }

    public List<Project> getDescendantProjects(String project, boolean strict, boolean allowInvalid)
    {
        Set<ProjectConfiguration> projectConfigs = configurationProvider.getAllDescendants(PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, project), ProjectConfiguration.class, strict, true);
        List<Long> ids = newArrayList(transform(projectConfigs, new Function<ProjectConfiguration, Long>()
        {
            public Long apply(ProjectConfiguration projectConfiguration)
            {
                return projectConfiguration.getProjectId();
            }
        }));

        return getProjects(ids, allowInvalid);
    }

    public boolean isProjectValid(ProjectConfiguration config)
    {
        return config != null && configurationTemplateManager.isDeeplyValid(config.getConfigurationPath());
    }

    private boolean isCompleteAndValid(Project project, boolean allowInvalid)
    {
        if (project == null)
        {
            return false;
        }

        if (project.getConfig() == null)
        {
            LOG.severe("Project '" + project.getId() + "' has no configuration.");
            return false;
        }

        return allowInvalid || configurationTemplateManager.isDeeplyValid(project.getConfig().getConfigurationPath());
    }

    private List<Project> filterCompleteAndValidProjects(List<Project> projects, final boolean allowInvalid)
    {
        return Lists.newArrayList(filter(projects, new Predicate<Project>()
        {
            public boolean apply(Project project)
            {
                return isCompleteAndValid(project, allowInvalid);
            }
        }));
    }

    public int getProjectCount(boolean includeInvalid)
    {
        return size(getProjects(includeInvalid));
    }

    public void abortUnfinishedBuilds(Project project, String message)
    {
        List<BuildResult> abortedBuilds = buildManager.abortUnfinishedBuilds(project, message);
        if (abortedBuilds.size() > 0)
        {
            project.setBuildCount(project.getBuildCount() + abortedBuilds.size());
            projectDao.save(project);
        }
    }

    public void cleanupWorkDirs(ProjectConfiguration projectConfig)
    {
        // We enqueue the cleanup messages synchronously here, so users using the remote API can
        // safely clean a project then trigger it (without the build being dispatched before the
        // cleanup messages are enqueued).
        workDirectoryCleanupService.enqueueCleanupMessages(projectConfig, null);
    }
    
    private void deleteProject(Project project)
    {
        projectInitialisationService.requestDestruction(project.getConfig(), true);
        buildManager.deleteAllBuilds(project);
        workDirectoryCleanupService.asyncEnqueueCleanupMessages(project.getConfig(), null);
        
        testCaseIndexDao.deleteByProject(project.getId());
        
        projectDao.delete(project);
    }

    public void delete(Project project)
    {
        makeStateTransition(project.getId(), Project.Transition.DELETE);
    }

    public List<Long> triggerBuild(ProjectConfiguration projectConfig, TriggerOptions options, Revision revision)
    {
        Project project = getProject(projectConfig.getProjectId(), false);

        // Rewire the project instance with the passed-in configuration, which may not be identical
        // to the persistent version (e.g. it could have additional properties).
        project.setConfig(projectConfig);

        List<Long> requestIds = new LinkedList<Long>();
        if(revision == null)
        {
            if(projectConfig.getOptions().getIsolateChangelists())
            {
                // In this case we need to check if there are multiple
                // outstanding revisions and if so create requests for each one.
                try
                {
                    Set<ScmCapability> capabilities = ScmClientUtils.getCapabilities(project.getConfig(), project.getState(), scmManager);
                    if (capabilities.contains(ScmCapability.REVISIONS))
                    {
                        List<Revision> revisions = changelistIsolator.getRevisionsToRequest(projectConfig, project, options.isForce());
                        for(Revision r: revisions)
                        {
                            // Note when isolating changelists we never replace existing requests
                            TriggerOptions copy = new TriggerOptions(options);
                            options.setReplaceable(false);
                            requestBuildOfRevision(project, copy, new BuildRevision(r, false), requestIds);
                        }
                    }
                    else
                    {
                        LOG.warning("Unable to use changelist isolation for project '" + projectConfig.getName() +
                                "' as the SCM does not support revisions");
                        requestBuildFloating(project, options, requestIds);
                    }
                }
                catch (ScmException e)
                {
                    LOG.error("Unable to determine revisions to build for project '" + projectConfig.getName() + "': " + e.getMessage(), e);
                }
            }
            else
            {
                requestBuildFloating(project, options, requestIds);
            }
        }
        else
        {
            // Just raise one request.
            requestBuildOfRevision(project, options, new BuildRevision(revision, options.getReason().isUser()), requestIds);
        }

        return requestIds;
    }

    public long triggerBuild(long number, Project project, User user, Revision revision, String reason, List<ResourcePropertyConfiguration> overrides, File patchFile, String patchFormat) throws PulseException
    {
        ProjectConfiguration projectConfig = getProjectConfig(project.getId(), false);
        if(projectConfig == null)
        {
            throw new PulseException("Unknown or invalid project configuration '" + project.getName() + "'");
        }

        try
        {
            BuildRevision buildRevision = revision == null ? new BuildRevision(new LatestScmRevisionSupplier(project, scmManager)): new BuildRevision(revision, false);
            BuildReason buildReason = StringUtils.stringSet(reason) ? new CustomBuildReason(reason) : new PersonalBuildReason(user.getLogin());
            return requestBuild(new PersonalBuildRequestEvent(this, number, buildRevision, user, patchFile, patchFormat, projectConfig, new TriggerOptions(buildReason, overrides)));
        }
        catch (Exception e)
        {
            throw new PulseException(e.getMessage(), e);
        }
    }

    private long requestBuild(BuildRequestEvent request)
 	{
 	    buildRequestRegistry.register(request);
 	 	eventManager.publish(request);
 	 	return request.getId();
 	}

    public long updateAndGetNextBuildNumber(Project project, boolean allocate)
    {
        Project idLeader = project;
        ProjectConfiguration idLeaderConfig = project.getConfig().getOptions().getIdLeader();
        if (idLeaderConfig != null)
        {
            idLeader = getProject(idLeaderConfig.getProjectId(), true);
        }

        lockProjectStates(project.getId(), idLeader.getId());
        try
        {
            project = getProject(project.getId(), true);
            idLeader = getProject(idLeader.getId(), true);

            BuildResult latest = buildManager.getLatestBuildResult(project, false);
            long minimum = latest == null ? 1 : latest.getNumber() + 1;

            long next = Math.max(minimum, idLeader.getNextBuildNumber());
            if (allocate)
            {
                idLeader.setNextBuildNumber(next + 1);
                save(idLeader);
            }
            return next;
        }
        finally
        {
            unlockProjectStates(project.getId(), idLeader.getId());
        }
    }

    private void requestBuildFloating(Project project, TriggerOptions options, List<Long> requestIds)
    {
        requestIds.add(requestBuild(new SingleBuildRequestEvent(this, project, new BuildRevision(new LatestScmRevisionSupplier(project, scmManager)), options)));
    }

    private void requestBuildOfRevision(Project project, TriggerOptions options, BuildRevision revision, List<Long> requestIds)
    {
        try
        {
            requestIds.add(requestBuild(new SingleBuildRequestEvent(this, project, revision, options)));
        }
        catch (Exception e)
        {
            LOG.severe("Unable to obtain pulse file for project '" + project.getName() + "', revision '" + revision.getRevision().getRevisionString() + "': " + e.getMessage(), e);
        }
    }

    private String mapTransitionToAction(Project.Transition transition)
    {
        String actionName = TRANSITION_TO_ACTION_MAP.get(transition);
        if (actionName == null)
        {
            actionName = AccessManager.ACTION_WRITE;
        }
        return actionName;
    }

    private void ensureTransitionPermission(Project project, Project.Transition transition)
    {
        accessManager.ensurePermission(mapTransitionToAction(transition), project);
    }
    
    public boolean hasStateTransitionPermission(Project project, Project.Transition transition)
    {
        return accessManager.hasPermission(mapTransitionToAction(transition), project);
    }

    public boolean makeStateTransition(long projectId, Project.Transition transition)
    {
        lockProjectStates(projectId);
        try
        {
            Project project = getProject(projectId, true);
            if (project == null)
            {
                return false;
            }

            ensureTransitionPermission(project, transition);

            if (project.isTransitionValid(transition))
            {
                Project.State state = project.stateTransition(transition);
                projectDao.save(project);

                switch (state)
                {
                    case INITIALISING:
                    {
                        projectInitialisationService.requestInitialisation(project.getConfig());
                        break;
                    }
                    case CLEANING:
                    {
                        projectInitialisationService.requestDestruction(project.getConfig(), false);
                        break;
                    }
                    case DELETING:
                    {
                        deleteProject(project);
                        break;
                    }
                }

                return true;
            }
            else
            {
                return false;
            }
        }
        finally
        {
            unlockProjectStates(projectId);
        }
    }

    public Collection<ProjectGroup> getAllProjectGroups()
    {
        final Comparator<String> cmp = new Sort.StringComparator();
        Set<ProjectGroup> groups = new TreeSet<ProjectGroup>(new Comparator<ProjectGroup>()
        {
            public int compare(ProjectGroup o1, ProjectGroup o2)
            {
                return cmp.compare(o1.getName(), o2.getName());
            }
        });

        cacheLock.readLock().lock();
        try
        {
            for(Map.Entry<String, Set<ProjectConfiguration>> entry: labelToConfigs.entrySet())
            {
                ProjectGroup group = createProjectGroup(entry.getKey(), entry.getValue());
                if(group.getProjects().size() > 0)
                {
                    groups.add(group);
                }
            }
        }
        finally
        {
            cacheLock.readLock().unlock();
        }

        return groups;
    }

    public ProjectGroup getProjectGroup(String name)
    {
        Set<ProjectConfiguration> projects;
        cacheLock.readLock().lock();
        try
        {
            projects = labelToConfigs.get(name);
        }
        finally
        {
            cacheLock.readLock().unlock();
        }

        if(projects == null)
        {
            // Return an empty group.
            return new ProjectGroup(name);
        }
        else
        {
            return createProjectGroup(name, projects);
        }
    }

    private ProjectGroup createProjectGroup(String name, Set<ProjectConfiguration> projectConfigs)
    {
        ProjectGroup group = new ProjectGroup(name);
        Actor actor = accessManager.getActor();
        for(ProjectConfiguration config: projectConfigs)
        {
            if(accessManager.hasPermission(actor, AccessManager.ACTION_VIEW, config))
            {
                Project project = projectDao.findById(config.getProjectId());
                if (ProjectPredicates.exists(project))
                {
                    group.add(project);
                }
            }
        }
        return group;
    }

    public List<Project> mapConfigsToProjects(Collection<ProjectConfiguration> projects)
    {
        List<Project> result = new LinkedList<Project>();
        for(ProjectConfiguration config: projects)
        {
            Project project = projectDao.findById(config.getProjectId());
            if(ProjectPredicates.exists(project))
            {
                result.add(project);
            }
        }

        return result;
    }

    public void removeReferencesToUser(User user)
    {
        for (Project project: projectDao.findByResponsible(user))
        {
            clearResponsibility(project);
        }
    }

    public void updateLastPollTime(long projectId, long timestamp)
    {
        lockProjectStates(projectId);
        try
        {
            Project project = getState(projectId);
            if (project != null)
            {
                project.setLastPollTime(timestamp);
                save(project);
            }
        }
        finally
        {
            unlockProjectStates(projectId);
        }
    }

    public List<Project> findByResponsible(User user)
    {
        return projectDao.findByResponsible(user);
    }

    public void takeResponsibility(Project project, User user, String comment)
    {
        project.setResponsibility(new ProjectResponsibility(user, comment));
        projectDao.save(project);
    }

    public void clearResponsibility(Project project)
    {
        project.setResponsibility(null);
        projectDao.save(project);
    }

    public List<ProjectConfiguration> getDownstreamDependencies(ProjectConfiguration projectConfig)
    {
        cacheLock.readLock().lock();
        try
        {
            Set<Long> result = configToDownstreamConfigHandles.get(projectConfig);
            return newArrayList(transform(result, new Function<Long, ProjectConfiguration>()
            {
                public ProjectConfiguration apply(Long handle)
                {
                    return configurationProvider.get(handle, ProjectConfiguration.class);
                }
            }));
        }
        finally
        {
            cacheLock.readLock().unlock();
        }
    }

    public void setConfiguration(Project state)
    {
        // We don't lock the cache here as this is a very low-level callback.
        // We rely on the concurrent map implementation to give us something
        // consistent back.
        ProjectConfiguration projectConfiguration = idToConfig.get(state.getId());
        state.setConfig(projectConfiguration);
    }

    private void handleBuildCompleted(BuildCompletedEvent event)
    {
        BuildResult buildResult = event.getBuildResult();
        if (!buildResult.isPersonal())
        {
            long projectId = buildResult.getProject().getId();
            lockProjectStates(projectId);
            try
            {
                Project project = getProject(projectId, true);
                if (project != null)
                {
                    project.setBuildCount(project.getBuildCount() + 1);
                    if (buildResult.healthy())
                    {
                        project.setSuccessCount(project.getSuccessCount() + 1);
                        if (project.getConfig().getOptions().isAutoClearResponsibility())
                        {
                            clearResponsibility(project);
                        }
                    }

                    projectDao.save(project);
                }
            }
            finally
            {
                unlockProjectStates(projectId);
            }
        }
    }

    private void handleInitialisationCompleted(ProjectInitialisationCompletedEvent event)
    {
        Project.Transition transition = event.isSuccessful() ? Project.Transition.INITIALISE_SUCCESS : Project.Transition.INITIALISE_FAILURE;
        makeStateTransition(event.getProjectConfiguration().getProjectId(), transition);
    }

    private void handleDestructionCompleted(ProjectDestructionCompletedEvent event)
    {
        ProjectConfiguration projectConfig = event.getProjectConfiguration();
        cleanupWorkDirs(projectConfig);
        // This will be ignored except in the cleaning case.
        makeStateTransition(projectConfig.getProjectId(), Project.Transition.CLEANED);
    }

    public void handleEvent(Event evt)
    {
        if (evt instanceof BuildCompletedEvent)
        {
            handleBuildCompleted((BuildCompletedEvent) evt);
        }
        else if (evt instanceof ProjectInitialisationCompletedEvent)
        {
            handleInitialisationCompleted((ProjectInitialisationCompletedEvent) evt);
        }
        else if (evt instanceof ProjectDestructionCompletedEvent)
        {
            handleDestructionCompleted((ProjectDestructionCompletedEvent) evt);
        }
        else if(evt instanceof ConfigurationEventSystemStartedEvent)
        {
            ConfigurationEventSystemStartedEvent cesse = (ConfigurationEventSystemStartedEvent) evt;
            registerConfigListener(cesse.getConfigurationProvider());
        }
        else if (evt instanceof ConfigurationSystemStartedEvent)
        {
            initialise();
        }
        else
        {
            LOG.severe("Project manager received unexpected event, ignoring: " + evt);
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[] { BuildCompletedEvent.class,
                             ProjectInitialisationCompletedEvent.class,
                             ProjectDestructionCompletedEvent.class,
                             ConfigurationEventSystemStartedEvent.class,
                             ConfigurationSystemStartedEvent.class };
    }

    public Map<Long, Revision> getLatestBuiltRevisions()
    {
        HashMap<Long, Revision> results = new HashMap<Long, Revision>();

        // we want the latest built revisions of all projects including invalid ones,
        // which may have been built in the past)
        List<Project> projects = getProjects(true);
        for (Project project : projects)
        {
            results.put(project.getId(), buildManager.getPreviousRevision(project));
        }
        return results;
    }

    public void clearResponsibilities(User user)
    {
        for (Project project : projectDao.findByResponsible(user))
        {
            clearResponsibility(project);
        }
    }

    public void setEventManager(EventManager eventManager)
    {
        eventManager.register(this);
        this.eventManager = eventManager;
    }

    public void setTestCaseIndexDao(TestCaseIndexDao testCaseIndexDao)
    {
        this.testCaseIndexDao = testCaseIndexDao;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public void setConfigurationStateManager(ConfigurationStateManager configurationStateManager)
    {
        configurationStateManager.register(ProjectConfiguration.class, this);
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setConfigurationInjector(ConfigurationInjector configurationInjector)
    {
        configurationInjector.registerSetter(Project.class, this);
    }

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }

    public void setAccessManager(AccessManager accessManager)
    {
        this.accessManager = accessManager;
    }

    public void setProjectInitialisationService(ProjectInitialisationService projectInitialisationService)
    {
        this.projectInitialisationService = projectInitialisationService;
    }

    public void setBuildRequestRegistry(BuildRequestRegistry buildRequestRegistry)
    {
        this.buildRequestRegistry = buildRequestRegistry;
    }

    public void setProjectDao(ProjectDao dao)
    {
        projectDao = dao;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setWorkDirectoryCleanupService(WorkDirectoryCleanupService workDirectoryCleanupService)
    {
        this.workDirectoryCleanupService = workDirectoryCleanupService;
    }
}
