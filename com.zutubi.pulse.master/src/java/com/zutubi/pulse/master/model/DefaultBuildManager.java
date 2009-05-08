package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.dependency.ivy.IvyClient;
import com.zutubi.pulse.core.dependency.ivy.IvyManager;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.MasterBuildPaths;
import com.zutubi.pulse.master.agent.MasterLocationProvider;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.bootstrap.WebManager;
import com.zutubi.pulse.master.cleanup.FileDeletionService;
import com.zutubi.pulse.master.database.DatabaseConsole;
import com.zutubi.pulse.master.model.persistence.ArtifactDao;
import com.zutubi.pulse.master.model.persistence.BuildResultDao;
import com.zutubi.pulse.master.model.persistence.ChangelistDao;
import com.zutubi.pulse.master.model.persistence.FileArtifactDao;
import com.zutubi.pulse.master.security.PulseThreadFactory;
import com.zutubi.pulse.master.security.RepositoryAuthenticationProvider;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.util.RandomUtils;
import com.zutubi.util.logging.Logger;
import org.apache.ivy.core.module.id.ModuleRevisionId;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * The build manager interface implementation.
 */
public class DefaultBuildManager implements BuildManager
{
    private static final Logger LOG = Logger.getLogger(DefaultBuildManager.class);

    private BuildResultDao buildResultDao;
    private ArtifactDao artifactDao;
    private FileArtifactDao fileArtifactDao;
    private ChangelistDao changelistDao;
    private MasterConfigurationManager configurationManager;
    private PulseThreadFactory threadFactory;
    private DatabaseConsole databaseConsole;

    private RepositoryAuthenticationProvider repositoryAuthenticationProvider;
    private FileDeletionService fileDeletionService;

    private IvyManager ivyManager;
    private MasterLocationProvider masterLocationProvider;

    public void init()
    {
        fileDeletionService = new FileDeletionService();
        fileDeletionService.setThreadFactory(threadFactory);
        fileDeletionService.init();

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

            for (File projectDir : projectDirs)
            {
                File[] deadDirs = projectDir.listFiles(new FileFilter()
                {
                    public boolean accept(File f)
                    {
                        return fileDeletionService.wasScheduledForDeletion(f);
                    }
                });

                for (File dead : deadDirs)
                {
                    scheduleCleanup(dead);
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

    public void save(PersistentChangelist changelist)
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
        fillHistoryPage(page, ResultState.getCompletedStates());
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
        if (isLatest(buildId))
        {
            return getLatestBuildResult(project);
        }
        else if (isLatestSuccessful(buildId))
        {
            return getLatestSuccessfulBuildResult(project);
        }
        else if (isLatestBroken(buildId))
        {
            return extractResult(queryBuilds(project, ResultState.getBrokenStates(), -1, -1, 1, 1, true, true));
        }
        else
        {
            try
            {
                long id = Long.parseLong(buildId);
                return getByProjectAndNumber(project, id);
            }
            catch (NumberFormatException e)
            {
                return null;
            }
        }
    }

    private BuildResult extractResult(List<BuildResult> results)
    {
        if (results.size() > 0)
        {
            return results.get(0);
        }
        else
        {
            return null;
        }
    }

    private boolean isLatest(String buildId)
    {
        return buildId.equals("latest");
    }

    private boolean isLatestSuccessful(String buildId)
    {
        return buildId.equals("success") || buildId.equals("successful") || buildId.equals("latestsuccess") || buildId.equals("latestsuccessful");
    }

    private boolean isLatestBroken(String buildId)
    {
        return buildId.equals("broken") || buildId.equals("latestbroken");
    }

    public BuildResult getByUserAndNumber(User user, long id)
    {
        return buildResultDao.findByUserAndNumber(user, id);
    }

    public BuildResult getByUserAndVirtualId(User user, String buildId)
    {
        if (isLatest(buildId))
        {
            return getLatestBuildResult(user);
        }
        else if (isLatestSuccessful(buildId))
        {
            return extractResult(buildResultDao.getLatestByUser(user, new ResultState[]{ResultState.SUCCESS}, 1));
        }
        else if (isLatestBroken(buildId))
        {
            return extractResult(buildResultDao.getLatestByUser(user, ResultState.getBrokenStates(), 1));
        }
        else
        {
            try
            {
                long id = Long.parseLong(buildId);
                return getByUserAndNumber(user, id);
            }
            catch (NumberFormatException e)
            {
                return null;
            }
        }
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
        return extractResult(buildResultDao.getLatestByUser(user, null, 1));
    }

    public List<BuildResult> queryBuilds(Project[] projects, ResultState[] states, long earliestStartTime, long latestStartTime, Boolean hasWorkDir, int first, int max, boolean mostRecentFirst)
    {
        return buildResultDao.queryBuilds(projects, states, earliestStartTime, latestStartTime, hasWorkDir, first, max, mostRecentFirst);
    }

    public List<BuildResult> queryBuilds(Project project, ResultState[] states, long lowestNumber, long highestNumber, int first, int max, boolean mostRecentFirst, boolean initialise)
    {
        return buildResultDao.queryBuilds(project, states, lowestNumber, highestNumber, first, max, mostRecentFirst, initialise);
    }

    public List<BuildResult> queryBuildsWithMessages(Project[] projects, Feature.Level level, int max)
    {
        return buildResultDao.queryBuildsWithMessages(projects, level, max);
    }

    public Revision getPreviousRevision(Project project)
    {
        Revision previousRevision = null;
        int offset = 0;

        while (true)
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

    public List<PersistentChangelist> getLatestChangesForUser(User user, int max)
    {
        return changelistDao.findLatestByUser(user, max);
    }

    public List<PersistentChangelist> getLatestChangesForProject(Project project, int max)
    {
        return changelistDao.findLatestByProject(project, max);
    }

    public List<PersistentChangelist> getLatestChangesForProjects(Project[] projects, int max)
    {
        return changelistDao.findLatestByProjects(projects, max);
    }

    public List<PersistentChangelist> getChangesForBuild(BuildResult result)
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
            for (BuildResult build : results)
            {
                process(build, BuildCleanupOptions.DATABASE_ONLY);
            }
        }
        while (results.size() > 0);
    }

    public void deleteAllBuilds(User user)
    {
        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        File userDir = paths.getUserDir(user.getId());
        scheduleCleanup(userDir);

        List<BuildResult> builds = buildResultDao.findByUser(user);
        for (BuildResult build : builds)
        {
            process(build, BuildCleanupOptions.DATABASE_ONLY);
        }

        builds = buildResultDao.findByResponsible(user);
        for (BuildResult result: builds)
        {
            clearResponsibility(result);
        }
    }

    public void delete(BuildResult result)
    {
        process(result, BuildCleanupOptions.ALL);
    }

    public List<BuildResult> abortUnfinishedBuilds(Project project, String message)
    {
        List<BuildResult> incompleteBuilds = queryBuilds(new Project[]{project}, ResultState.getIncompleteStates(), -1, -1, null, -1, -1, true);
        for (BuildResult r : incompleteBuilds)
        {
            abortBuild(r, message);
        }
        return incompleteBuilds;
    }

    public void abortUnfinishedBuilds(User user, String message)
    {
        List<BuildResult> incompleteBuilds = buildResultDao.getLatestByUser(user, ResultState.getIncompleteStates(), -1);
        for (BuildResult r : incompleteBuilds)
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
        return !databaseConsole.isEmbedded() || databaseConsole.getDatabaseUsagePercent() < 99.5;
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

    public void process(BuildResult build, BuildCleanupOptions request)
    {
        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);

        if (request.isCleanBuildDir())
        {
            File buildDir = paths.getBuildDir(build);
            if (buildDir.exists())
            {
                scheduleCleanup(buildDir);
            }
        }

        if (request.isCleanWorkDir())
        {
            build.setHasWorkDir(false);
            buildResultDao.save(build);
            cleanupWorkForNodes(paths, build, build.getRoot().getChildren());
        }

        if (request.isCleanDatabase())
        {
            if (build.isPersonal())
            {
                File patch = paths.getUserPatchFile(build.getUser().getId(), build.getNumber());
                scheduleCleanup(patch);
            }
            else
            {
                // Remove records of this build from changelists
                Revision revision = build.getRevision();
                if (revision != null)
                {
                    List<PersistentChangelist> changelists = changelistDao.findByResult(build.getId());
                    for (PersistentChangelist change : changelists)
                    {
                        changelistDao.delete(change);
                    }
                }
            }

            try
            {
                // clean artifact repository artifacts for this build.
                for (File f : getRepositoryFilesFor(build))
                {
                    scheduleCleanup(f);
                }
            }
            catch (Exception e)
            {
                LOG.warning(e);
            }

            buildResultDao.delete(build);
        }
    }

    private List<File> getRepositoryFilesFor(BuildResult build) throws Exception
    {
        List<File> repositoryFiles = new LinkedList<File>();

        String securityToken = RandomUtils.randomToken(15);
        try
        {
            repositoryAuthenticationProvider.activate(securityToken);

            // provide the authentication details for the subsquent repository requests.
            String masterLocation = masterLocationProvider.getMasterUrl();
            String host = new URL(masterLocation).getHost();

            // the reponse from the ivy client will be relative to the repository root.
            File repositoryRoot = configurationManager.getUserPaths().getRepositoryRoot();

            IvyClient ivy = ivyManager.createIvyClient(masterLocation + WebManager.REPOSITORY_PATH);
            ivy.addCredentials(host, "pulse", securityToken);

            ProjectConfiguration project = build.getProject().getConfig();
            ModuleRevisionId mrid = ModuleRevisionId.newInstance(project.getOrganisation(), project.getName(), String.valueOf(build.getNumber()));

            List<String> artifacts = ivy.getArtifactPaths(mrid);
            if (artifacts != null)
            {
                for (String relativePath : artifacts)
                {
                    File file = new File(repositoryRoot, relativePath);
                    repositoryFiles.add(file);
                    repositoryFiles.addAll(findRelatedFiles(file));
                }
            }
            File file = new File(repositoryRoot, ivy.getIvyPath(mrid));
            repositoryFiles.add(file);
            repositoryFiles.addAll(findRelatedFiles(file));
        }
        finally
        {
            repositoryAuthenticationProvider.deactivate(securityToken);
        }

        return repositoryFiles;
    }

    private List<File> findRelatedFiles(final File file)
    {
        return Arrays.asList(file.getParentFile().listFiles(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name.startsWith(file.getName());
            }
        }));
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
        fileDeletionService.delete(dir);
    }

    public List<BuildResult> findByResponsible(User user)
    {
        return buildResultDao.findByResponsible(user);
    }

    public void takeResponsibility(BuildResult build, User user, String comment)
    {
        BuildResponsibility responsibility = new BuildResponsibility(user, comment);
        build.setResponsibility(responsibility);
        buildResultDao.save(build);
    }

    public void clearResponsibility(BuildResult build)
    {
        build.setResponsibility(null);
        buildResultDao.save(build);
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

    public void setIvyManager(IvyManager ivyManager)
    {
        this.ivyManager = ivyManager;
    }

    public void setMasterLocationProvider(MasterLocationProvider masterLocationProvider)
    {
        this.masterLocationProvider = masterLocationProvider;
    }

    public void setRepositoryAuthenticationProvider(RepositoryAuthenticationProvider repositoryAuthenticationProvider)
    {
        this.repositoryAuthenticationProvider = repositoryAuthenticationProvider;
    }
}
