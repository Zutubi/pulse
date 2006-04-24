/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.model;

import com.zutubi.pulse.MasterBuildPaths;
import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.bootstrap.DatabaseBootstrap;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.build.BuildCompletedEvent;
import com.zutubi.pulse.model.persistence.ArtifactDao;
import com.zutubi.pulse.model.persistence.BuildResultDao;
import com.zutubi.pulse.model.persistence.ChangelistDao;
import com.zutubi.pulse.model.persistence.FileArtifactDao;
import com.zutubi.pulse.scheduling.Scheduler;
import com.zutubi.pulse.scheduling.SchedulingException;
import com.zutubi.pulse.scheduling.SimpleTrigger;
import com.zutubi.pulse.scheduling.Trigger;
import com.zutubi.pulse.scheduling.tasks.CleanupBuilds;
import com.zutubi.pulse.util.Constants;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.logging.Logger;

import java.io.File;
import java.util.List;

/**
 * 
 *
 */
public class DefaultBuildManager implements BuildManager, EventListener
{
    private static final Logger LOG = Logger.getLogger(DefaultBuildManager.class);

    // Forces the dependency in spring (CIB-166)
    private DatabaseBootstrap databaseBootstrap;
    private BuildResultDao buildResultDao;
    private ArtifactDao artifactDao;
    private FileArtifactDao fileArtifactDao;
    private ChangelistDao changelistDao;
    private ProjectManager projectManager;
    private Scheduler scheduler;
    private ConfigurationManager configurationManager;

    private static final String CLEANUP_NAME = "cleanup";
    private static final String CLEANUP_GROUP = "services";
    private static final long CLEANUP_FREQUENCY = Constants.HOUR;

    public void init()
    {
        // register a schedule for cleaning up old build results.
        // check if the trigger exists. if not, create and schedule.
        Trigger trigger = scheduler.getTrigger(CLEANUP_NAME, CLEANUP_GROUP);
        if (trigger != null)
        {
            return;
        }

        // initialise the trigger.
        trigger = new SimpleTrigger(CLEANUP_NAME, CLEANUP_GROUP, CLEANUP_FREQUENCY);
        trigger.setTaskClass(CleanupBuilds.class);

        try
        {
            scheduler.schedule(trigger);
        }
        catch (SchedulingException e)
        {
            LOG.severe(e);
        }
    }

    public void setBuildResultDao(BuildResultDao dao)
    {
        buildResultDao = dao;
    }

    public void setArtifactDao(ArtifactDao dao)
    {
        artifactDao = dao;
    }

    public void save(BuildResult buildResult)
    {
        buildResultDao.save(buildResult);
    }

    public void save(RecipeResultNode node)
    {
        buildResultDao.save(node);
    }

    public void save(RecipeResult result)
    {
        buildResultDao.save(result);
    }

    public BuildResult getBuildResult(long id)
    {
        return buildResultDao.findById(id);
    }

    public RecipeResultNode getRecipeResultNode(long id)
    {
        return buildResultDao.findRecipeResultNode(id);
    }

    public RecipeResult getRecipeResult(long id)
    {
        return buildResultDao.findRecipeResult(id);
    }

    public List<BuildResult> getLatestBuildResultsForProject(Project project, int max)
    {
        return buildResultDao.findLatestByProject(project, max);
    }

    public int getBuildCount(Project project, ResultState[] states, String spec)
    {
        return buildResultDao.getBuildCount(project, states, spec);
    }

    public void fillHistoryPage(HistoryPage page)
    {
        fillHistoryPage(page, new ResultState[]{ResultState.ERROR, ResultState.FAILURE, ResultState.SUCCESS}, null);
    }

    public List<String> getBuildSpecifications(Project project)
    {
        return buildResultDao.findAllSpecifications(project);
    }

    public void fillHistoryPage(HistoryPage page, ResultState[] states, String spec)
    {
        page.setTotalBuilds(buildResultDao.getBuildCount(page.getProject(), states, spec));
        page.setResults(buildResultDao.findLatestByProject(page.getProject(), states, spec, page.getFirst(), page.getMax()));
    }

    public List<BuildResult> getLatestCompletedBuildResults(Project project, String spec, int max)
    {
        return buildResultDao.findLatestCompleted(project, spec, max);
    }

    public List<BuildResult> getOldestBuildsForProject(Project project, int max)
    {
        return buildResultDao.findOldestByProject(project, max);
    }

    public List<BuildResult> getOldestBuildsForProject(Project project, int first, int max)
    {
        return buildResultDao.findOldestByProject(project, first, max);
    }

    public BuildResult getLatestBuildResult(Project project)
    {
        List<BuildResult> results = getLatestBuildResultsForProject(project, 1);
        if (results.size() > 0)
        {
            return results.get(0);
        }
        return null;
    }

    public BuildResult getByProjectAndNumber(final Project project, final long number)
    {
        return buildResultDao.findByProjectAndNumber(project, number);
    }

    public CommandResult getCommandResult(long id)
    {
        return buildResultDao.findCommandResult(id);
    }

    public StoredArtifact getArtifact(long id)
    {
        return artifactDao.findById(id);
    }

    public StoredFileArtifact getFileArtifact(long id)
    {
        return fileArtifactDao.findById(id);
    }

    public List<BuildResult> queryBuilds(Project[] projects, ResultState[] states, String[] specs, long earliestStartTime, long latestStartTime, Boolean hasWorkDir, int first, int max, boolean mostRecentFirst)
    {
        return buildResultDao.queryBuilds(projects, states, specs, earliestStartTime, latestStartTime, hasWorkDir, first, max, mostRecentFirst);
    }

    public long getNextBuildNumber(Project project)
    {
        long number = 1;
        List<BuildResult> builds = getLatestBuildResultsForProject(project, 1);
        BuildResult previousBuildResult;

        if (builds.size() > 0)
        {
            previousBuildResult = builds.get(0);
            number = previousBuildResult.getNumber() + 1;
        }
        return number;
    }

    public void cleanupBuilds()
    {
        // Lookup project cleanup info, query for old builds, cleanup where necessary
        List<Project> projects = projectManager.getAllProjects();
        for (Project project : projects)
        {
            cleanupBuilds(project);
        }
    }

    public List<Changelist> getLatestChangesForUser(User user, int max)
    {
        return changelistDao.findLatestByUser(user, max);
    }

    public List<Changelist> getLatestChangesForProject(Project project, int max)
    {
        return changelistDao.findLatestByProject(project, max);
    }

    public void deleteAllBuilds(Project project)
    {
        int offset = 0;
        List<BuildResult> results;

        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        File projectDir = paths.getProjectDir(project);
        if (!FileSystemUtils.removeDirectory(projectDir))
        {
            LOG.warning("Unable to remove project directory '" + projectDir.getAbsolutePath() + "'");
        }

        do
        {
            results = buildResultDao.findOldestByProject(project, offset, 100);
            for (BuildResult r : results)
            {
                buildResultDao.delete(r);
            }
            offset += results.size();
        }
        while (results.size() > 0);
    }

    public Changelist getChangelistByRevision(String serverUid, Revision revision)
    {
        return changelistDao.findByRevision(serverUid, revision);
    }

    public BuildResult getPreviousBuildResult(BuildResult result)
    {
        return buildResultDao.findPreviousBuildResult(result);
    }

    private synchronized void cleanupBuilds(Project project)
    {
        List<CleanupRule> rules = project.getCleanupRules();

        for (CleanupRule rule : rules)
        {
            List<BuildResult> oldBuilds = rule.getMatchingResults(project, buildResultDao);

            for (BuildResult build : oldBuilds)
            {
                if (rule.getWorkDirOnly())
                {
                    cleanupWork(project, build);
                }
                else
                {
                    cleanupResult(project, build);
                }
            }
        }
    }

    private void cleanupResult(Project project, BuildResult build)
    {
        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        File buildDir = paths.getBuildDir(project, build);
        if (!FileSystemUtils.removeDirectory(buildDir))
        {
            LOG.warning("Unable to clean up build directory '" + buildDir.getAbsolutePath() + "'");
            return;
        }

        buildResultDao.delete(build);
    }

    private void cleanupWork(Project project, BuildResult build)
    {
        build.setHasWorkDir(false);
        buildResultDao.save(build);
        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        cleanupWorkForNodes(paths, project, build, build.getRoot().getChildren());
    }

    private void cleanupWorkForNodes(MasterBuildPaths paths, Project project, BuildResult build, List<RecipeResultNode> nodes)
    {
        for (RecipeResultNode node : nodes)
        {
            File workDir = paths.getBaseDir(project, build, node.getResult().getId());
            if (!FileSystemUtils.removeDirectory(workDir))
            {
                LOG.warning("Unable to clean up build directory '" + workDir.getAbsolutePath() + "'");
            }

            cleanupWorkForNodes(paths, project, build, node.getChildren());
        }
    }

    public void handleEvent(Event evt)
    {
        BuildCompletedEvent completedEvent = (BuildCompletedEvent) evt;
        cleanupBuilds(completedEvent.getResult().getProject());
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{BuildCompletedEvent.class};
    }

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setChangelistDao(ChangelistDao changelistDao)
    {
        this.changelistDao = changelistDao;
    }

    public void setDatabaseBootstrap(DatabaseBootstrap databaseBootstrap)
    {
        this.databaseBootstrap = databaseBootstrap;
    }

    public void setFileArtifactDao(FileArtifactDao fileArtifactDao)
    {
        this.fileArtifactDao = fileArtifactDao;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
