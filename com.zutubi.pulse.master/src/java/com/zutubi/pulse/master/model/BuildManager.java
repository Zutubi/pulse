package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.security.SecureParameter;
import com.zutubi.pulse.master.security.SecureResult;
import com.zutubi.tove.security.AccessManager;

import java.util.List;

/**
 * Handles high level management of build results.
 */
public interface BuildManager
{
    void save(BuildResult result);

    void save(RecipeResultNode node);

    void save(RecipeResult result);

    /**
     * Retrieves a build result by its database id.
     *
     * @param id id of the build to find
     * @return the found build, or null if there is no such build
     */
    @SecureResult
    BuildResult getBuildResult(long id);

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

    @SecureResult
    List<BuildResult> getPersonalBuilds(User user);

    @SecureResult
    BuildResult getLatestBuildResult(User user);

    @SecureResult
    BuildResult getLatestBuildResult();

    /**
     * Queries for build results with the given criteria.  Personal builds are not included.  The first and max
     * parameters may be used to page through results, and mostRecentFirst controls the order in which they are
     * returned.
     *
     * @param projects          if not empty, only include builds of these projects
     * @param states            if not empty, only include builds in these states
     * @param earliestStartTime if greater than zero, limit to builds with a start time greater than or equal to this
     * @param latestStartTime   if greater than zero, limit to builds with a start time less than or equal to this
     * @param first             if non-negative, zero-based offset of the first build to return
     * @param max               if non-negative, the maximum number of results to return (the actual number may be less)
     * @param mostRecentFirst   if true, return more recent (by id, therefore activation time) results first
     * @return a list of build results that meet the given criteria
     */
    @SecureResult
    List<BuildResult> queryBuilds(Project[] projects, ResultState[] states, long earliestStartTime, long latestStartTime, int first, int max, boolean mostRecentFirst);

    @SecureResult
    List<BuildResult> queryBuilds(Project project, ResultState[] states, long lowestNumber, long highestNumber, int first, int max, boolean mostRecentFirst, boolean initialise);

    @SecureResult
    List<BuildResult> getLatestBuildResultsForProject(Project project, int max);

    @SecureParameter(parameterType = Project.class, action = AccessManager.ACTION_VIEW)
    int getBuildCount(Project project, ResultState[] states);

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

    int getBuildCount(Agent agent, ResultState[] states);

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

    /**
     * Returns all builds for the given projects that completed after the given
     * time stamp.
     *
     * @param projects  the projects to find builds of
     * @param sinceTime time in milliseconds since the epoch to restrict
     *                  completed times to
     * @return all completed builds of the given projects with completion times
     *         after the given time
     */
    @SecureResult
    List<BuildResult> getBuildsCompletedSince(Project[] projects, long sinceTime);

    /**
     * Retrieves the latest build result for a given project in any one of a given set of states.
     *
     * @param project    the project to get a build from
     * @param initialise if true, the build is fully loaded from the database (i.e. no lazy
     *                   collections)
     * @param inStates   set of states to restrict the build to
     * @return the latest build of the given project in one of the given states, or null if there
     *         is no such build
     */
    @SecureResult
    BuildResult getLatestBuildResult(Project project, boolean initialise, ResultState... inStates);

    @SecureResult
    BuildResult getLatestBuildResult(ResultState... inStates);

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
     * Analogous to {@link #getByProjectAndVirtualId(Project, String)} , but used to find a
     * personal rather than a project build.
     *
     * @param user    the user to find the personal build for
     * @param buildId the real or virtual build id (see
     *                {@link #getByProjectAndVirtualId(Project, String)})
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
     * Retrieve the build result that occurred immediately before the specified build result.
     *
     * @param result a build to find the previous build for
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
     * @return  the latest non-user-defined revision used to build the project, or null if there is
     *          no such revision
     */
    @SecureParameter(action = AccessManager.ACTION_VIEW)
    Revision getPreviousRevision(Project project);

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

    @SecureParameter(parameterType = BuildResult.class, action = AccessManager.ACTION_WRITE)
    boolean togglePin(BuildResult buildResult, boolean pin);

    @SecureParameter(parameterType = Project.class, action = AccessManager.ACTION_WRITE)
    List<BuildResult> abortUnfinishedBuilds(Project project, String message);

    @SecureParameter(parameterType = User.class, action = AccessManager.ACTION_WRITE)
    void abortUnfinishedBuilds(User user, String message);

    boolean isSpaceAvailableForBuild();

    CommandResult getCommandResultByArtifact(long artifactId);

    CommandResult getCommandResult(long id, String commandName);

    StoredArtifact getArtifact(long buildId, String artifactName);

    StoredArtifact getCommandResultByArtifact(long commandResultId, String artifactName);

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
     * @param reason        the human readable reason for the termination request
     * @param kill          if true, kill the build as quickly as possible with
     *                      no graceful cleanup
     */
    void terminateBuild(BuildResult buildResult, String reason, boolean kill);

    /**
     * Requests all running builds terminate.  This requires admin permission.
     *
     * @param reason a human-readable reason for the request
     * @param kill   if true, kill the builds as quickly as possible with
     *               no graceful cleanup
     */
    void terminateAllBuilds(String reason, boolean kill);
}
