/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
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

    BuildResult getBuildResult(long id);

    RecipeResultNode getRecipeResultNode(long id);

    RecipeResult getRecipeResult(long id);

    CommandResult getCommandResult(long id);

    StoredArtifact getArtifact(long id);

    StoredFileArtifact getFileArtifact(long id);

    List<BuildResult> queryBuilds(Project[] projects, ResultState[] states, String[] specs, long earliestStartTime, long latestStartTime, Boolean hasWorkDir, int first, int max, boolean mostRecentFirst);

    List<BuildResult> getLatestBuildResultsForProject(Project project, int max);

    public int getBuildCount(Project project, ResultState[] states, String spec);

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
     * @param spec   if no null, restrict to results of the given spec
     */
    void fillHistoryPage(HistoryPage page, ResultState[] states, String spec);

    /**
     * @param project the project to search for
     * @return all build specification names referred to by build results of
     *         the given project
     */
    List<String> getBuildSpecifications(Project project);

    List<BuildResult> getLatestCompletedBuildResults(Project project, String spec, int max);

    BuildResult getLatestBuildResult(Project project);

    BuildResult getByProjectAndNumber(final Project project, final long number);

    /**
     * Retrieve the build result that occured immediately before the specified build result.
     *
     * @param result
     *
     * @return a build result or null if the specified build result is the first.
     */
    BuildResult getPreviousBuildResult(BuildResult result);

    long getNextBuildNumber(Project project);

    void cleanupBuilds();

    /**
     * Returns the most recent changelists submitted by the given user.
     *
     * @param user the user to get the changelists for
     * @param max  the maximum number of results to return
     * @return a list of up to max of the most recent changes for the user
     */
    List<Changelist> getLatestChangesForUser(User user, int max);

    List<Changelist> getLatestChangesForProject(Project project, int max);

    void deleteAllBuilds(Project project);

    Changelist getChangelistByRevision(String serverUid, Revision revision);
}
