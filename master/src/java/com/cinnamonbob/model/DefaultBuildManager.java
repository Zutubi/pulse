package com.cinnamonbob.model;

import com.cinnamonbob.MasterBuildPaths;
import com.cinnamonbob.core.model.CommandResult;
import com.cinnamonbob.core.model.RecipeResult;
import com.cinnamonbob.core.model.StoredArtifact;
import com.cinnamonbob.core.util.Constants;
import com.cinnamonbob.core.util.FileSystemUtils;
import com.cinnamonbob.model.persistence.ArtifactDao;
import com.cinnamonbob.model.persistence.BuildResultDao;
import com.cinnamonbob.scheduling.Scheduler;
import com.cinnamonbob.util.logging.Logger;

import java.io.File;
import java.util.List;

/**
 * 
 *
 */
public class DefaultBuildManager implements BuildManager
{
    private static final Logger LOG = Logger.getLogger(DefaultBuildManager.class);

    private BuildResultDao buildResultDao;
    private ArtifactDao artifactDao;
    private ProjectManager projectManager;
    private Scheduler scheduler;

    private static final String CLEANUP_NAME = "cleanup";
    private static final String CLEANUP_GROUP = "services";
    private static final long CLEANUP_FREQUENCY = Constants.DAY;

    public void init()
    {
//        // register a schedule for cleaning up old build results.
//        // check if the trigger exists. if not, create and schedule.
//        Trigger trigger = scheduler.getTrigger(CLEANUP_NAME, CLEANUP_GROUP);
//        if (trigger != null)
//        {
//            return;
//        }
//
//        // initialise the trigger.
//        trigger = new SimpleTrigger(CLEANUP_NAME, CLEANUP_GROUP, CLEANUP_FREQUENCY);
//        trigger.setTaskClass(CleanupBuilds.class);
//
//        try
//        {
//            scheduler.schedule(trigger);
//        }
//        catch (SchedulingException e)
//        {
//            LOG.severe(e);
//        }
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

    public List<BuildResult> getLatestCompletedBuildResults(Project project, BuildSpecification spec, int max)
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

    private void cleanupBuilds(Project project)
    {
        BuildResultCleanupPolicy policy = project.getCleanupPolicy();
        boolean done = false;
        int offset = 0;

        while (!done)
        {
            List<BuildResult> oldBuilds = getOldestBuildsForProject(project, offset, 10);

            if (oldBuilds.size() == 0)
            {
                break;
            }

            for (BuildResult build : oldBuilds)
            {
                if (policy.canCleanupResult(build))
                {
                    cleanupResult(project, build);
                }
                else if (policy.canCleanupWorkDir(build))
                {
                    cleanupWork(project, build);
                }
                else
                {
                    // We cannot do any more: this is assumes that an older
                    // result not being cleaned up implies that a younger one
                    // also will not.  For more complicated policies this may
                    // break, but in that case we need a better querying strategy
                    // to avoid having to walk all build results.
                    done = true;
                    break;
                }
            }

            offset += 10;
        }
    }

    private void cleanupResult(Project project, BuildResult build)
    {
        MasterBuildPaths paths = new MasterBuildPaths();
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
        MasterBuildPaths paths = new MasterBuildPaths();
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

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
