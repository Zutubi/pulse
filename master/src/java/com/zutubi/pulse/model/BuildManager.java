package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.*;

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

    void save(Changelist changelist);

    BuildResult getBuildResult(long id);

    RecipeResultNode getRecipeResultNode(long id);

    RecipeResultNode getResultNodeByResultId(long id);

    RecipeResult getRecipeResult(long id);

    CommandResult getCommandResult(long id);

    StoredArtifact getArtifact(long id);

    StoredFileArtifact getFileArtifact(long id);

    List<BuildResult> getPersonalBuilds(User user);

    BuildResult getLatestBuildResult(User user);

    BuildResult getLatestBuildResult();
    
    List<BuildResult> queryBuilds(Project[] projects, ResultState[] states, long earliestStartTime, long latestStartTime, Boolean hasWorkDir, int first, int max, boolean mostRecentFirst);

    List<BuildResult> queryBuilds(Project project, ResultState[] states, long lowestNumber, long highestNumber, int first, int max, boolean mostRecentFirst, boolean initialise);

    List<BuildResult> getLatestBuildResultsForProject(Project project, int max);

    public int getBuildCount(Project project, ResultState[] states);

    /**
     * Counts the number of builds that have happened subsequent to the given
     * build number for the given spec.
     *
     * @param after lower number for the count range, not inclusive
     * @param upTo  upper number of the count range, inclusive
     */
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

    List<BuildResult> getLatestCompletedBuildResults(Project project, int max);

    List<BuildResult> getLatestCompletedBuildResults(Project project, int first, int max);

    BuildResult getLatestBuildResult(Project project);

    BuildResult getLatestSuccessfulBuildResult(Project project);

    BuildResult getLatestSuccessfulBuildResult();

    BuildResult getByProjectAndNumber(final Project project, final long number);

    BuildResult getByUserAndNumber(User user, long id);

    /**
     * Retrieve the build result that occured immediately before the specified build result.
     *
     * @param result
     *
     * @return a build result or null if the specified build result is the first.
     */
    BuildResult getPreviousBuildResult(BuildResult result);

    Revision getPreviousRevision(Project project);

    /**
     * Returns the most recent changelists submitted by the given user.
     *
     * @param user the user to get the changelists for
     * @param max  the maximum number of results to return
     * @return a list of up to max of the most recent changes for the user
     */
    List<Changelist> getLatestChangesForUser(User user, int max);

    List<Changelist> getLatestChangesForProject(Project project, int max);

    List<Changelist> getLatestChangesForProjects(Project[] projects, int max);

    List<Changelist> getChangesForBuild(BuildResult result);

    void deleteAllBuilds(Project project);

    void deleteAllBuilds(User user);

    Changelist getChangelistByRevision(String serverUid, Revision revision);

    void delete(BuildResult result);

    List<BuildResult> abortUnfinishedBuilds(Project project, String message);

    void abortUnfinishedBuilds(User user, String message);

    boolean isSpaceAvailableForBuild();

    boolean canCancel(BuildResult build, User user);

    CommandResult getCommandResultByArtifact(long artifactId);

    CommandResult getCommandResult(long id, String commandName);

    StoredArtifact getArtifact(long buildId, String artifactName);

    StoredArtifact getCommandResultByArtifact(long commandResultId, String artifactName);

    Boolean canDecorateArtifact(long artifactId);

    // debugging hack: need to work out a better way
    void executeInTransaction(Runnable r);

    void cleanupResult(BuildResult build, boolean rmdir);

    void cleanupWork(BuildResult build);
}
