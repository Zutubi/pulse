package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.security.SecureParameter;
import com.zutubi.pulse.master.security.SecureResult;
import com.zutubi.tove.security.AccessManager;

import java.util.List;

/**
 * 
 *
 */
public interface BuildManager
{
    void save(BuildResult result);

    void save(RecipeResultNode node);

    void save(RecipeResult result);

    void save(PersistentChangelist changelist);

    @SecureResult
    BuildResult getBuildResult(long id);

    RecipeResultNode getRecipeResultNode(long id);

    RecipeResultNode getResultNodeByResultId(long id);

    /**
     * Finds the build that contains a specified recipe result, if any.
     *
     * @param id id of the recipe result to find the containing build for
     * @return the build containing the recipe, or null if not found
     */
    @SecureResult
    BuildResult getByRecipeId(long id);

    RecipeResult getRecipeResult(long id);

    CommandResult getCommandResult(long id);

    StoredArtifact getArtifact(long id);

    StoredFileArtifact getFileArtifact(long id);

    @SecureResult
    List<BuildResult> getPersonalBuilds(User user);

    @SecureResult
    BuildResult getLatestBuildResult(User user);

    @SecureResult
    BuildResult getLatestBuildResult();

    @SecureResult
    List<BuildResult> queryBuilds(Project[] projects, ResultState[] states, long earliestStartTime, long latestStartTime, Boolean hasWorkDir, int first, int max, boolean mostRecentFirst);

    @SecureResult
    List<BuildResult> queryBuildsWithMessages(Project[] projects, Feature.Level level, int max);

    @SecureResult
    List<BuildResult> queryBuilds(Project project, ResultState[] states, long lowestNumber, long highestNumber, int first, int max, boolean mostRecentFirst, boolean initialise);

    @SecureResult
    List<BuildResult> getLatestBuildResultsForProject(Project project, int max);

    @SecureParameter(parameterType = Project.class, action = AccessManager.ACTION_VIEW)
    public int getBuildCount(Project project, ResultState[] states);

    /**
     * Counts the number of builds that have happened between the given build numbers.
     *
     * @param project   the project in question
     * @param after     lower number for the count range, not inclusive
     * @param upTo      upper number of the count range, inclusive
     *
     * @return the number of builds
     */
    @SecureParameter(parameterType = Project.class, action = AccessManager.ACTION_VIEW)
    int getBuildCount(Project project, long after, long upTo);

    /**
     * Fills out the list in the given history page based on the page offset.
     * Results in the initial state are NOT included.
     *
     * @param page the page to fill in
     */
    void fillHistoryPage(HistoryPage page);

    /**
     * Fills out the list in the given history page based on the given
     * criteria and the offset of the page.
     *
     * @param page   the page to fill in
     * @param states if not null, restrict to results in one of these states
     */
    void fillHistoryPage(HistoryPage page, ResultState[] states);

    /**
     * Get the build result for the latest completed build for the specified
     * project.  If no completed build is available, null is returned.
     *
     * Completed builds are those for which the {@link com.zutubi.pulse.core.engine.api.ResultState#isCompleted()}
     * returns true. 
     *
     * @param project   the project being queried.
     * @return  the latest completed build result, or null.
     */
    @SecureResult
    BuildResult getLatestCompletedBuildResult(Project project);

    @SecureResult
    List<BuildResult> getLatestCompletedBuildResults(Project project, int max);

    @SecureResult
    List<BuildResult> getLatestCompletedBuildResults(Project project, int first, int max);

    @SecureResult
    BuildResult getLatestBuildResult(Project project);

    @SecureResult
    BuildResult getLatestSuccessfulBuildResult(Project project);

    @SecureResult
    BuildResult getLatestSuccessfulBuildResult();

    @SecureResult
    BuildResult getByProjectAndNumber(final Project project, final long number);

    /**
     * Returns the given build for the given project.  The build id can be
     * either a numeric build id or one of a few recognised virtual ids:
     *
     *  latest              : the latest build
     *  [latest]success[ful]: the latest successful build
     *  [latest]broken      : the latest unsuccessful build
     *  
     * @param project the project to lookup the build for
     * @param buildId the real or virtual build id
     * @return the described build, or null if there is no such build
     */
    @SecureResult
    BuildResult getByProjectAndVirtualId(Project project, String buildId);

    @SecureResult
    BuildResult getByUserAndNumber(User user, long id);

    /**
     * Analogous to {@link #getByProjectAndVirtualId} , but used to find a
     * personal rather than a project build.
     *
     * @param user    the user to find the personal build for
     * @param buildId the real or virtual build id (see
     *                {@link #getByProjectAndVirtualId})
     * @return the described build, or null if there is no such build
     */
    @SecureResult
    BuildResult getByUserAndVirtualId(User user, String buildId);

    /**
     * Get the build result for the specified project and metabuild.
     *
     * @param project       the project associated with the build
     * @param metaBuildId   the metabuild that the build was a part of.
     *
     * @return the build result for the project build assocaited with the metabuild, or
     * null if it is not found.
     */
    @SecureResult
    BuildResult getByProjectAndMetabuildId(Project project, long metaBuildId);

    /**
     * Retrieve the build result that occured immediately before the specified build result.
     *
     * @param result
     *
     * @return a build result or null if the specified build result is the first.
     */
    @SecureResult
    BuildResult getPreviousBuildResult(BuildResult result);

    /**
     * Retrieve the latest build result from before the given result that has a
     * non-user build revision and a state in the given set.
     *
     * @param result the result to search before
     * @param states the set of acceptable states for the previous build (may
     *               be null to indicate not to restrict by state)
     * @return a build result or null if no results meet the criteria
     */
    @SecureResult
    BuildResult getPreviousBuildResultWithRevision(BuildResult result, ResultState[] states);

    /**
     * Returns the latest non user revision used to run a build for the
     * specified project.
     *
     * @param project   the project of interest.
     * @return  the latest non user defined revision used to build the
     * project.  
     */
    @SecureParameter(action = AccessManager.ACTION_VIEW)
    Revision getPreviousRevision(Project project);

    /**
     * Returns the most recent changelists submitted by the given user.
     *
     * @param user the user to get the changelists for
     * @param max  the maximum number of results to return
     * @return a list of up to max of the most recent changes for the user
     */
    @SecureParameter(parameterType = User.class, action = AccessManager.ACTION_VIEW)
    List<PersistentChangelist> getLatestChangesForUser(User user, int max);

    @SecureParameter(parameterType = Project.class, action = AccessManager.ACTION_VIEW)
    List<PersistentChangelist> getLatestChangesForProject(Project project, int max);

    @SecureParameter(parameterType = Project.class, action = AccessManager.ACTION_VIEW)
    List<PersistentChangelist> getLatestChangesForProjects(Project[] projects, int max);

    @SecureParameter(action = AccessManager.ACTION_VIEW)
    List<PersistentChangelist> getChangesForBuild(BuildResult result);

    @SecureParameter(action = AccessManager.ACTION_WRITE)
    void deleteAllBuilds(Project project);

    @SecureParameter(action = AccessManager.ACTION_WRITE)
    void deleteAllBuilds(User user);

    /**
     * Delete the specified build result along with any persisted artifacts from the
     * build.  This is equivalent to calling {@link #cleanup(BuildResult, BuildCleanupOptions)}
     * with all of the cleanup options set to true.
     *
     * @param result    the build result to be deleted.
     */
    @SecureParameter(action = AccessManager.ACTION_WRITE)
    void delete(BuildResult result);

    @SecureParameter(parameterType = Project.class, action = AccessManager.ACTION_WRITE)
    List<BuildResult> abortUnfinishedBuilds(Project project, String message);

    @SecureParameter(parameterType = User.class, action = AccessManager.ACTION_WRITE)
    void abortUnfinishedBuilds(User user, String message);

    boolean isSpaceAvailableForBuild();

    CommandResult getCommandResultByArtifact(long artifactId);

    CommandResult getCommandResult(long id, String commandName);

    StoredArtifact getArtifact(long buildId, String artifactName);

    StoredArtifact getCommandResultByArtifact(long commandResultId, String artifactName);

    Boolean canDecorateArtifact(long artifactId);

    /**
     * This method allows a build result to be cleaned up.  The details of exactly which
     * portions of a build result are to be cleaned up is defined within the options parameter.
     *
     * @param result    the build result to be cleaned up.
     * @param options   the options detailing exactly what is to be cleaned up.
     *
     * @see #delete(BuildResult)
     */
    @SecureParameter(parameterType = BuildResult.class, action = AccessManager.ACTION_WRITE)
    void cleanup(BuildResult result, BuildCleanupOptions options);

    /**
     * Request that the specified build be terminated.
     *
     * @param buildResult   the build to terminate
     * @param reason        the human readable reason for the termination request.
     */
    void terminateBuild(BuildResult buildResult, String reason);
}
