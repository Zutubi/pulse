package com.zutubi.pulse.model;

import com.zutubi.pulse.MasterBuildPaths;
import com.zutubi.pulse.bootstrap.DatabaseConsole;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.model.persistence.ArtifactDao;
import com.zutubi.pulse.model.persistence.BuildResultDao;
import com.zutubi.pulse.model.persistence.ChangelistDao;
import com.zutubi.pulse.model.persistence.FileArtifactDao;
import com.zutubi.pulse.security.PulseThreadFactory;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 *
 */
public class DefaultBuildManager implements BuildManager
{
    private static final Logger LOG = Logger.getLogger(DefaultBuildManager.class);

    private static final String DEAD_DIR_SUFFIX = ".dead";

    private BuildResultDao buildResultDao;
    private ArtifactDao artifactDao;
    private FileArtifactDao fileArtifactDao;
    private ChangelistDao changelistDao;
    private ProjectManager projectManager;
    private MasterConfigurationManager configurationManager;
    private PulseThreadFactory threadFactory;
    private DatabaseConsole databaseConsole;

    private BlockingQueue<CleanupRequest> cleanupQueue = new LinkedBlockingQueue<CleanupRequest>();

    public void init()
    {
        Thread cleanupThread = threadFactory.newThread(new Runnable()
        {
            @SuppressWarnings({"InfiniteLoopStatement"})
            public void run()
            {
                while(true)
                {
                    try
                    {
                        CleanupRequest request = cleanupQueue.take();
                        if(request.dir.exists() && !FileSystemUtils.rmdir(request.dir))
                        {
                            LOG.warning("Unable to remove directory '" + request.dir.getAbsolutePath() + "'");
                        }
                    }
                    catch (InterruptedException e)
                    {
                        LOG.warning(e);
                    }
                }
            }
        }, "Build Cleanup Service");

        cleanupThread.setDaemon(true);
        cleanupThread.start();

        // CIB-1147: detect and remove old .dead dirs on restart.
        cleanupDeadDirectories();
    }

    private void cleanupDeadDirectories()
    {
        File projectRoot = configurationManager.getUserPaths().getProjectRoot();
        if (projectRoot.isDirectory())
        {
            File[] projectDirs = projectRoot.listFiles(new FileFilter()
            {
                public boolean accept(File f)
                {
                    return f.isDirectory();
                }
            });

            for(File projectDir: projectDirs)
            {
                File[] deadDirs = projectDir.listFiles(new FileFilter()
                {
                    public boolean accept(File f)
                    {
                        return f.isDirectory() && f.getName().endsWith(DEAD_DIR_SUFFIX);
                    }
                });

                for(File dead: deadDirs)
                {
                    scheduleDeadCleanup(dead);
                }
            }
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

    public void save(Changelist changelist)
    {
        changelistDao.save(changelist);
    }

    public BuildResult getBuildResult(long id)
    {
        return buildResultDao.findById(id);
    }

    public RecipeResultNode getRecipeResultNode(long id)
    {
        return buildResultDao.findRecipeResultNode(id);
    }

    public RecipeResultNode getResultNodeByResultId(long id)
    {
        return buildResultDao.findResultNodeByResultId(id);
    }

    public RecipeResult getRecipeResult(long id)
    {
        return buildResultDao.findRecipeResult(id);
    }

    public List<BuildResult> getLatestBuildResultsForProject(Project project, int max)
    {
        return buildResultDao.findLatestByProject(project, max);
    }

    public int getBuildCount(Project project, ResultState[] states)
    {
        return buildResultDao.getBuildCount(project, states);
    }

    public int getBuildCount(Project project, long after, long upTo)
    {
        return buildResultDao.getBuildCount(project, after, upTo);
    }

    public void fillHistoryPage(HistoryPage page)
    {
        fillHistoryPage(page, new ResultState[]{ResultState.ERROR, ResultState.FAILURE, ResultState.SUCCESS});
    }

    public void fillHistoryPage(HistoryPage page, ResultState[] states)
    {
        page.setTotalBuilds(buildResultDao.getBuildCount(page.getProject(), states));
        page.setResults(buildResultDao.findLatestByProject(page.getProject(), states, page.getFirst(), page.getMax()));
    }

    public List<BuildResult> getLatestCompletedBuildResults(Project project, int max)
    {
        return getLatestCompletedBuildResults(project, 0, max);
    }

    public List<BuildResult> getLatestCompletedBuildResults(Project project, int first, int max)
    {
        return buildResultDao.findLatestCompleted(project, first, max);
    }

    public BuildResult getByProjectAndNumber(final Project project, final long number)
    {
        return buildResultDao.findByProjectAndNumber(project, number);
    }

    public BuildResult getByProjectAndVirtualId(Project project, String buildId)
    {
        if(buildId.equals("latest"))
        {
            return getLatestBuildResult(project);
        }
        else if(buildId.equals("success") || buildId.equals("successful") || buildId.equals("latestsuccess") || buildId.equals("latestsuccessful"))
        {
            return getLatestSuccessfulBuildResult(project);
        }
        else if(buildId.equals("broken") || buildId.equals("latestbroken"))
        {
            List<BuildResult> results = queryBuilds(project, new ResultState[]{ResultState.ERROR, ResultState.FAILURE}, -1, -1, 1, 1, true, true);
            if(results.size() > 0)
            {
                return results.get(0);
            }
            else
            {
                return null;
            }
        }
        else
        {
            try
            {
                long id = Long.parseLong(buildId);
                return getByProjectAndNumber(project, id);
            }
            catch(NumberFormatException e)
            {
                return null;
            }
        }
    }

    public BuildResult getByUserAndNumber(User user, long id)
    {
        return buildResultDao.findByUserAndNumber(user, id);
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

    public List<BuildResult> getPersonalBuilds(User user)
    {
        return buildResultDao.findByUser(user);
    }

    public BuildResult getLatestBuildResult(User user)
    {
        List<BuildResult> results = buildResultDao.getLatestByUser(user, null, 1);
        if(results.size() > 0)
        {
            return results.get(0);
        }
        else
        {
            return null;
        }
    }

    public List<BuildResult> queryBuilds(Project[] projects, ResultState[] states, long earliestStartTime, long latestStartTime, Boolean hasWorkDir, int first, int max, boolean mostRecentFirst)
    {
        return buildResultDao.queryBuilds(projects, states, earliestStartTime, latestStartTime, hasWorkDir, first, max, mostRecentFirst);
    }

    public List<BuildResult> queryBuilds(Project project, ResultState[] states, long lowestNumber, long highestNumber, int first, int max, boolean mostRecentFirst, boolean initialise)
    {
        return buildResultDao.queryBuilds(project, states, lowestNumber, highestNumber, first, max, mostRecentFirst, initialise);
    }

    public Revision getPreviousRevision(Project project)
    {
        Revision previousRevision = null;
        int offset = 0;

        while(true)
        {
            List<BuildResult> previousBuildResults = getLatestCompletedBuildResults(project, offset, 1);

            if (previousBuildResults.size() == 1)
            {
                BuildResult previous = previousBuildResults.get(0);
                if (!previous.isUserRevision())
                {
                    previousRevision = previous.getRevision();
                    if (previousRevision != null)
                    {
                        break;
                    }
                }
            }
            else
            {
                break;
            }

            offset++;
        }

        return previousRevision;
    }

    public List<Changelist> getLatestChangesForUser(User user, int max)
    {
        return changelistDao.findLatestByUser(user, max);
    }

    public List<Changelist> getLatestChangesForProject(Project project, int max)
    {
        return changelistDao.findLatestByProject(project, max);
    }

    public List<Changelist> getLatestChangesForProjects(Project[] projects, int max)
    {
        return changelistDao.findLatestByProjects(projects, max);
    }

    public List<Changelist> getChangesForBuild(BuildResult result)
    {
        return changelistDao.findByResult(result.getId());
    }

    public void deleteAllBuilds(Project project)
    {
        List<BuildResult> results;

        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        File projectDir = paths.getProjectDir(project);
        scheduleCleanup(projectDir);

        do
        {
            results = buildResultDao.findOldestByProject(project, null, 100, true);
            for (BuildResult r : results)
            {
                cleanupResult(r, false);
            }
        }
        while (results.size() > 0);
    }

    public void deleteAllBuilds(User user)
    {
        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        File userDir = paths.getUserDir(user.getId());
        scheduleCleanup(userDir);

        List<BuildResult> results = buildResultDao.findByUser(user);
        for (BuildResult r : results)
        {
            cleanupResult(r, false);
        }
    }

    public void delete(BuildResult result)
    {
        cleanupResult(result, true);
    }

    public List<BuildResult> abortUnfinishedBuilds(Project project, String message)
    {
        List<BuildResult> incompleteBuilds = queryBuilds(new Project[]{ project}, ResultState.getIncompleteStates(), -1, -1, null, -1, -1, true);
        for(BuildResult r: incompleteBuilds)
        {
            abortBuild(r, message);
        }
        return incompleteBuilds;
    }

    public void abortUnfinishedBuilds(User user, String message)
    {
        List<BuildResult> incompleteBuilds = buildResultDao.getLatestByUser(user, ResultState.getIncompleteStates(), -1);
        for(BuildResult r: incompleteBuilds)
        {
            abortBuild(r, message);
        }
    }

    private void abortBuild(BuildResult build, String message)
    {
        if (build != null && !build.completed())
        {
            build.abortUnfinishedRecipes();
            build.error(message);
            build.complete();
            build.calculateFeatureCounts();
            save(build);
        }
    }

    public boolean isSpaceAvailableForBuild()
    {
        if (databaseConsole.isEmbedded())
        {
            return databaseConsole.getDatabaseUsagePercent() < 99.5;
        }
        return true;
    }

    public BuildResult getPreviousBuildResult(BuildResult result)
    {
        return buildResultDao.findPreviousBuildResult(result);
    }

    public CommandResult getCommandResultByArtifact(long artifactId)
    {
        return buildResultDao.findCommandResultByArtifact(artifactId);
    }

    public CommandResult getCommandResult(long recipeResultId, String commandName)
    {
        return buildResultDao.findRecipeResult(recipeResultId).getCommandResult(commandName);
    }

    public StoredArtifact getArtifact(long buildId, String artifactName)
    {
        BuildResult result = buildResultDao.findById(buildId);
        return result.findArtifact(artifactName);
    }

    public StoredArtifact getCommandResultByArtifact(long commandResultId, String artifactName)
    {
        return buildResultDao.findCommandResult(commandResultId).getArtifact(artifactName);
    }

    public Boolean canDecorateArtifact(long artifactId)
    {
        StoredFileArtifact artifact = getFileArtifact(artifactId);
        return artifact != null && artifact.canDecorate();
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

    public BuildResult getLatestBuildResult()
    {
        return buildResultDao.findLatest();
    }

    public BuildResult getLatestSuccessfulBuildResult(Project project)
    {
        return buildResultDao.findLatestSuccessfulByProject(project);
    }

    public BuildResult getLatestSuccessfulBuildResult()
    {
        return buildResultDao.findLatestSuccessful();
    }

    public void cleanupResult(BuildResult build, boolean rmdir)
    {
        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        File buildDir = paths.getBuildDir(build);
        if (rmdir && buildDir.exists())
        {
            scheduleCleanup(buildDir);
        }

        if(build.isPersonal())
        {
            File patch = paths.getUserPatchFile(build.getUser().getId(), build.getNumber());
            patch.delete();
        }
        else
        {
            // Remove records of this build from changelists
            Revision revision = build.getRevision();
            if(revision != null)
            {
                List<Changelist> changelists = changelistDao.findByResult(build.getId());
                for(Changelist change: changelists)
                {
                    changelistDao.delete(change);
                }
            }
        }

        buildResultDao.delete(build);
    }

    public void cleanupWork(BuildResult build)
    {
        build.setHasWorkDir(false);
        buildResultDao.save(build);
        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        cleanupWorkForNodes(paths, build, build.getRoot().getChildren());
    }

    public void executeInTransation(Runnable runnable)
    {
        runnable.run();
    }

    private void cleanupWorkForNodes(MasterBuildPaths paths, BuildResult build, List<RecipeResultNode> nodes)
    {
        for (RecipeResultNode node : nodes)
        {
            File workDir = paths.getBaseDir(build, node.getResult().getId());
            if (workDir.exists())
            {
                scheduleCleanup(workDir);
            }
            cleanupWorkForNodes(paths, build, node.getChildren());
        }
    }

    private void scheduleCleanup(File dir)
    {
        File dead = new File(dir + DEAD_DIR_SUFFIX);
        dir.renameTo(dead);
        scheduleDeadCleanup(dead);
    }

    private void scheduleDeadCleanup(File dead)
    {
        try
        {
            cleanupQueue.put(new CleanupRequest(dead));
        }
        catch (InterruptedException e)
        {
            LOG.warning(e);
        }
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setChangelistDao(ChangelistDao changelistDao)
    {
        this.changelistDao = changelistDao;
    }

    public void setFileArtifactDao(FileArtifactDao fileArtifactDao)
    {
        this.fileArtifactDao = fileArtifactDao;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setDatabaseConsole(DatabaseConsole databaseConsole)
    {
        this.databaseConsole = databaseConsole;
    }

    public void setThreadFactory(PulseThreadFactory threadFactory)
    {
        this.threadFactory = threadFactory;
    }

    private class CleanupRequest
    {
        private File dir;

        public CleanupRequest(File dir)
        {
            this.dir = dir;
        }
    }
}
