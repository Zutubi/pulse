package com.zutubi.pulse.master.model;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.dependency.RepositoryAttributes;
import com.zutubi.pulse.core.dependency.ivy.IvyConfiguration;
import com.zutubi.pulse.core.dependency.ivy.IvyEncoder;
import com.zutubi.pulse.core.dependency.ivy.IvyModuleDescriptor;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.MasterBuildPaths;
import com.zutubi.pulse.master.agent.MasterLocationProvider;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.bootstrap.WebManager;
import com.zutubi.pulse.master.build.log.BuildLogFile;
import com.zutubi.pulse.master.build.log.LogFile;
import com.zutubi.pulse.master.build.log.RecipeLogFile;
import com.zutubi.pulse.master.database.DatabaseConsole;
import com.zutubi.pulse.master.dependency.ivy.MasterIvyModuleRevisionId;
import com.zutubi.pulse.master.events.build.BuildTerminationRequestEvent;
import com.zutubi.pulse.master.model.persistence.ArtifactDao;
import com.zutubi.pulse.master.model.persistence.BuildResultDao;
import com.zutubi.pulse.master.model.persistence.ChangelistDao;
import com.zutubi.pulse.master.model.persistence.FileArtifactDao;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;
import com.zutubi.pulse.servercore.cleanup.FileDeletionService;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.Predicate;
import com.zutubi.util.io.IsDirectoryPredicate;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.zutubi.pulse.core.dependency.RepositoryAttributePredicates.attributeEquals;
import static com.zutubi.pulse.core.dependency.RepositoryAttributes.PROJECT_HANDLE;

/**
 * The build manager interface implementation.
 */
public class DefaultBuildManager implements BuildManager
{
    private static final Logger LOG = Logger.getLogger(DefaultBuildManager.class);

    private AccessManager accessManager;
    private EventManager eventManager;
    private BuildResultDao buildResultDao;
    private ArtifactDao artifactDao;
    private FileArtifactDao fileArtifactDao;
    private ChangelistDao changelistDao;
    private MasterConfigurationManager configurationManager;
    private DatabaseConsole databaseConsole;

    private FileDeletionService fileDeletionService;

    private MasterLocationProvider masterLocationProvider;
    private RepositoryAttributes repositoryAttributes;

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

    public BuildResult getByRecipeId(long id)
    {
        return buildResultDao.findByRecipeId(id);
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

    public void fillHistoryPage(HistoryPage page, ResultState[] states)
    {
        page.setTotalBuilds(buildResultDao.getBuildCount(page.getProject(), states));
        page.setResults(buildResultDao.findLatestByProject(page.getProject(), states, page.getFirst(), page.getMax()));
    }

    public BuildResult getLatestCompletedBuildResult(Project project)
    {
        List<BuildResult> results = getLatestCompletedBuildResults(project, 1);
        if (results.size() > 0)
        {
            return results.get(0);
        }
        return null;
    }

    public List<BuildResult> getLatestCompletedBuildResults(Project project, int max)
    {
        return getLatestCompletedBuildResults(project, 0, max);
    }

    public List<BuildResult> getLatestCompletedBuildResults(Project project, int first, int max)
    {
        return buildResultDao.findLatestCompleted(project, first, max);
    }

    public List<BuildResult> getBuildsCompletedSince(Project[] projects, long sinceTime)
    {
        return buildResultDao.findCompletedSince(projects, sinceTime);
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

    public BuildResult getByProjectAndMetabuildId(Project project, long metaBuildId)
    {
        return buildResultDao.findByProjectAndMetabuildId(project, metaBuildId);
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

    public List<BuildResult> queryBuilds(Project[] projects, ResultState[] states, long earliestStartTime, long latestStartTime, int first, int max, boolean mostRecentFirst)
    {
        return buildResultDao.queryBuilds(projects, states, earliestStartTime, latestStartTime, first, max, mostRecentFirst);
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

    public List<PersistentChangelist> getChangesForBuild(BuildResult result, boolean allowEmpty)
    {
        return changelistDao.findByResult(result.getId(), allowEmpty);
    }

    public int getChangelistSize(PersistentChangelist changelist)
    {
        return changelistDao.getSize(changelist);
    }
    
    public List<PersistentFileChange> getChangelistFiles(PersistentChangelist changelist, int offset, int max)
    {
        return changelistDao.getFiles(changelist, offset, max);
    }

    public void deleteAllBuilds(Project project)
    {
        List<BuildResult> results;

        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        File projectDir = paths.getProjectDir(project);
        scheduleCleanup(projectDir);

        try
        {
            for (File dir: getRepositoryDirectoriesFor(project))
            {
                scheduleCleanup(dir);
            }
        }
        catch (Exception e)
        {
            LOG.severe("Unable to locate and delete artifact repository directories for project '" + project.getName() + "'");
        }

        // Files on disk are cleaned by removing project-level directories, so
        // we just remove the builds from the database.
        do
        {
            results = buildResultDao.findOldestByProject(project, null, 100, true);
            for (BuildResult build : results)
            {
                cleanupDatabase(build);
            }
        }
        while (results.size() > 0);
    }

    public void deleteAllBuilds(User user)
    {
        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        File userDir = paths.getUserDir(user.getId());
        scheduleCleanup(userDir);

        // Files on disk are cleaned by removing user-level directories, so
        // we just remove the builds from the database.
        List<BuildResult> builds = buildResultDao.findByUser(user);
        for (BuildResult build : builds)
        {
            cleanupDatabase(build);
        }
    }

    public void delete(BuildResult result)
    {
        cleanup(result, new BuildCleanupOptions(true));
    }

    public List<BuildResult> abortUnfinishedBuilds(Project project, String message)
    {
        List<BuildResult> incompleteBuilds = queryBuilds(new Project[]{project}, ResultState.getIncompleteStates(), -1, -1, -1, -1, true);
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

    public BuildResult getPreviousBuildResultWithRevision(BuildResult result, ResultState[] states)
    {
        return buildResultDao.findPreviousBuildResultWithRevision(result, states);
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

    public void cleanup(BuildResult build, BuildCleanupOptions options)
    {
        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);

        if (options.isCleanupAll()) // then we are cleaning up everything in the build directory.
        {
            if (build.isPersonal())
            {
                File patch = paths.getUserPatchFile(build.getUser().getId(), build.getNumber());
                scheduleCleanup(patch);
            }
            else
            {
                cleanupBuildDirectory(build);
            }
        }
        else // we are only cleaning up portions of the build directory. 
        {
            if (options.isCleanBuildArtifacts())
            {
                cleanupBuildArtifacts(build);
            }

            if (options.isCleanupLogs())
            {
                cleanupBuildLogs(build);
            }
        }

        if (options.isCleanRepositoryArtifacts() && !build.isPersonal())
        {
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
        }

        if (options.isCleanupAll())
        {
            cleanupDatabase(build);
        }
    }

    private void cleanupDatabase(BuildResult build)
    {
        if (!build.isPersonal())
        {
            // Remove records of this build from changelists
            Revision revision = build.getRevision();
            if (revision != null)
            {
                List<PersistentChangelist> changelists = changelistDao.findByResult(build.getId(), true);
                for (PersistentChangelist change : changelists)
                {
                    changelistDao.delete(change);
                }
            }
        }
        
        buildResultDao.delete(build);
    }

    public void terminateBuild(BuildResult buildResult, String reason)
    {
        accessManager.ensurePermission(ProjectConfigurationActions.ACTION_CANCEL_BUILD, buildResult);
        eventManager.publish(new BuildTerminationRequestEvent(this, buildResult.getId(), reason));
    }

    private List<File> getRepositoryDirectoriesFor(final Project project) throws Exception
    {
        final File repositoryRoot = configurationManager.getUserPaths().getRepositoryRoot();
        List<String> paths = repositoryAttributes.getPaths(attributeEquals(PROJECT_HANDLE, String.valueOf(project.getConfig().getHandle())));
        return CollectionUtils.filter(CollectionUtils.map(paths, new Mapping<String, File>()
        {
            public File map(String s)
            {
                return new File(repositoryRoot, s);
            }
        }), new IsDirectoryPredicate());
    }
    
    private List<File> getRepositoryFilesFor(final BuildResult build) throws Exception
    {
        final List<File> repositoryFiles = new LinkedList<File>();

        // provide the authentication details for the subsequent repository requests.
        String masterLocation = masterLocationProvider.getMasterUrl();

        // the response from the ivy client will be relative to the repository root.
        final File repositoryRoot = configurationManager.getUserPaths().getRepositoryRoot();

        IvyConfiguration configuration = new IvyConfiguration(masterLocation + WebManager.REPOSITORY_PATH);

        String candidateIvyPath = configuration.getIvyPath(IvyEncoder.encode(MasterIvyModuleRevisionId.newInstance(build)), build.getVersion());
        File candidateIvyFile = new File(repositoryRoot, candidateIvyPath);
        if (!candidateIvyFile.isFile())
        {
            List<String> paths = repositoryAttributes.getPaths(attributeEquals(PROJECT_HANDLE, String.valueOf(build.getProject().getConfig().getHandle())));

            // file the ivy file.
            candidateIvyPath = CollectionUtils.find(paths, new Predicate<String>()
            {
                public boolean satisfied(String path)
                {
                    return new File(repositoryRoot, path + "/ivy-" + build.getVersion() + ".xml").isFile();
                }
            });
            candidateIvyFile = new File(repositoryRoot, candidateIvyPath + "/ivy-" + build.getVersion() + ".xml");
        }

        if (candidateIvyFile.isFile())
        {
            IvyModuleDescriptor ivyModuleDescriptor = IvyModuleDescriptor.newInstance(candidateIvyFile, configuration);
            List<String> artifacts = ivyModuleDescriptor.getArtifactPaths();
            if (artifacts != null)
            {
                for (String relativePath : artifacts)
                {
                    File file = new File(repositoryRoot, relativePath);
                    repositoryFiles.add(file);
                    repositoryFiles.addAll(findRelatedFiles(file));
                }
            }
            repositoryFiles.add(candidateIvyFile);
            repositoryFiles.addAll(findRelatedFiles(candidateIvyFile));
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

    public void setRepositoryAttributes(RepositoryAttributes repositoryAttributes)
    {
        this.repositoryAttributes = repositoryAttributes;
    }

    public void setAccessManager(AccessManager accessManager)
    {
        this.accessManager = accessManager;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setFileDeletionService(FileDeletionService fileDeletionService)
    {
        this.fileDeletionService = fileDeletionService;
    }

    /**
     * A simple callback interface used to aid the recipe cleanup process.  Implementations
     * of this interface will clean up specific portions of a recipe.
     */
    private interface RecipeCleanup
    {
        /**
         * Cleanup the specified recipe
         *
         * @param recipe    the recipe to be cleaned.
         */
        void cleanup(RecipeResult recipe);
    }

    /**
     * Recurse over the list of nodes and there children, executing the recipe clean for each
     * of the nodes.
     *
     * @param nodes     list of nodes to be traversed.
     * @param cleanup   the cleanup process to be applied to each recipe result.
     */
    private void runCleanupForRecipes(List<RecipeResultNode> nodes, RecipeCleanup cleanup)
    {
        for (RecipeResultNode node : nodes)
        {
            cleanup.cleanup(node.getResult());
            runCleanupForRecipes(node.getChildren(), cleanup);
        }
    }

    /**
     * Cleanup the output and features directories from each of the nodes.
     *
     * @param build     the build for which all of the artifact directories will be cleaned.
     */
    private void cleanupBuildArtifacts(final BuildResult build)
    {
        final MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        runCleanupForRecipes(build.getRoot().getChildren(), new RecipeCleanup()
        {
            public void cleanup(RecipeResult recipe)
            {
                scheduleCleanup(paths.getOutputDir(build, recipe.getId()));
                scheduleCleanup(paths.getFeaturesDir(build, recipe.getId()));
            }
        });
    }

    private void cleanupBuildDirectory(BuildResult build)
    {
        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        scheduleCleanup(paths.getBuildDir(build));
    }

    /**
     * Cleanup the build.log and recipe.log files associated with this build.
     *
     * @param build     the build for which the logs are being removed.
     */
    private void cleanupBuildLogs(final BuildResult build)
    {
        final MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        new BuildLogFile(build, paths).scheduleCleanup(fileDeletionService);

        runCleanupForRecipes(build.getRoot().getChildren(), new RecipeCleanup()
        {
            public void cleanup(RecipeResult recipe)
            {
                LogFile recipeLog = new RecipeLogFile(build, recipe.getId(), paths);
                recipeLog.scheduleCleanup(fileDeletionService);
            }
        });
    }

    private void scheduleCleanup(File file)
    {
        if (file != null && file.exists())
        {
            fileDeletionService.delete(file, false);
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

    public void setMasterLocationProvider(MasterLocationProvider masterLocationProvider)
    {
        this.masterLocationProvider = masterLocationProvider;
    }
}
