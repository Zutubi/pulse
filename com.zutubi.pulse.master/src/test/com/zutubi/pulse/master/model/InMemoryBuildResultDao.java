package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.master.model.persistence.BuildResultDao;
import com.zutubi.pulse.master.model.persistence.InMemoryEntityDao;

import java.util.Date;
import java.util.List;

/**
 * Testing implementation of {@link BuildResultDao} that keeps a set of builds in memory.  Note
 * that this implementation only supports operations needed for testing thus far.
 */
public class InMemoryBuildResultDao extends InMemoryEntityDao<BuildResult> implements BuildResultDao
{
    public void save(RecipeResultNode node)
    {
        throw new RuntimeException("Not yet implemented");
    }

    public void save(RecipeResult result)
    {
        throw new RuntimeException("Not yet implemented");
    }

    public void save(CommandResult result)
    {
        throw new RuntimeException("Not yet implemented");
    }

    public List<BuildResult> queryBuilds(Project[] projects, ResultState[] states, String[] statuses, long earliestStartTime, long latestStartTime, int first, int max, boolean mostRecentFirst, boolean includePinned)
    {
        throw new RuntimeException("Not yet implemented");
    }

    public List<BuildResult> queryBuilds(Project[] projects, ResultState[] states, long earliestStartTime, long latestStartTime, int first, int max, boolean mostRecentFirst)
    {
        throw new RuntimeException("Not yet implemented");
    }

    public List<BuildResult> queryBuildsWithMessages(Project[] projects, Feature.Level level, int max)
    {
        throw new RuntimeException("Not yet implemented");
    }

    public List<BuildResult> findLatestByProject(Project project, int max)
    {
        throw new RuntimeException("Not yet implemented");
    }

    public List<BuildResult> findSinceByProject(Project project, Date since)
    {
        throw new RuntimeException("Not yet implemented");
    }

    public List<BuildResult> findLatestByProject(Project project, int first, int max)
    {
        throw new RuntimeException("Not yet implemented");
    }

    public List<BuildResult> findLatestByProject(Project project, ResultState[] states, int first, int max)
    {
        throw new RuntimeException("Not yet implemented");
    }

    public List<BuildResult> findLatestCompleted(Project project, int first, int max)
    {
        throw new RuntimeException("Not yet implemented");
    }

    public List<BuildResult> findCompletedSince(Project[] projects, long sinceTime)
    {
        throw new RuntimeException("Not yet implemented");
    }

    public BuildResult findPreviousBuildResult(BuildResult result)
    {
        throw new RuntimeException("Not yet implemented");
    }

    public BuildResult findPreviousBuildResultWithRevision(BuildResult result, ResultState[] states)
    {
        throw new RuntimeException("Not yet implemented");
    }

    public List<BuildResult> findOldestByProject(Project project, ResultState[] states, int max, boolean includePersonal)
    {
        throw new RuntimeException("Not yet implemented");
    }

    public BuildResult findByProjectAndNumber(long projectId, long number)
    {
        throw new RuntimeException("Not yet implemented");
    }

    public BuildResult findByUserAndNumber(User user, long id)
    {
        throw new RuntimeException("Not yet implemented");
    }

    public CommandResult findCommandResult(long id)
    {
        throw new RuntimeException("Not yet implemented");
    }

    public RecipeResult findRecipeResult(long id)
    {
        throw new RuntimeException("Not yet implemented");
    }

    public int getBuildCount(Project project, ResultState[] states)
    {
        throw new RuntimeException("Not yet implemented");
    }

    public int getBuildCount(Project[] projects, ResultState[] states)
    {
        throw new RuntimeException("Not yet implemented");
    }

    public int getBuildCountByAgentName(String agent, Project[] projects, ResultState[] states)
    {
        throw new RuntimeException("Not yet implemented");
    }

    public int getBuildCount(Project project, ResultState[] states, String[] statuses, boolean includePinned)
    {
        throw new RuntimeException("Not yet implemented");
    }

    public int getBuildCount(Project project, long after, long upTo)
    {
        throw new RuntimeException("Not yet implemented");
    }

    public List<BuildResult> queryBuilds(Project project, ResultState[] states, long lowestNumber, long highestNumber, int first, int max, boolean mostRecentFirst, boolean initialise)
    {
        throw new RuntimeException("Not yet implemented");
    }

    public List<BuildResult> findByUser(User user)
    {
        throw new RuntimeException("Not yet implemented");
    }

    public List<BuildResult> getLatestByUser(User user, ResultState[] states, int max)
    {
        throw new RuntimeException("Not yet implemented");
    }

    public int getCompletedResultCount(User user)
    {
        throw new RuntimeException("Not yet implemented");
    }

    public List<BuildResult> getOldestCompletedBuilds(User user, int offset)
    {
        throw new RuntimeException("Not yet implemented");
    }

    public RecipeResultNode findResultNodeByResultId(long id)
    {
        throw new RuntimeException("Not yet implemented");
    }

    public BuildResult findLatest(ResultState... inStates)
    {
        throw new RuntimeException("Not yet implemented");
    }

    public CommandResult findCommandResultByArtifact(long artifactId)
    {
        throw new RuntimeException("Not yet implemented");
    }

    public BuildResult findLatestByProject(Project project, boolean initialise, ResultState... inStates)
    {
        throw new RuntimeException("Not yet implemented");
    }

    public List<BuildResult> findLatestByAgentName(String agent, Project[] projects, ResultState[] states, int first, int max)
    {
        throw new RuntimeException("Not yet implemented");
    }

    public BuildResult findByRecipeId(long id)
    {
        throw new RuntimeException("Not yet implemented");
    }

    public BuildResult findByProjectAndMetabuildId(Project project, long metaBuildId, ResultState... states)
    {
        throw new RuntimeException("Not yet implemented");
    }

    public List<BuildResult> findByBeforeBuild(long buildId, int maxResults, ResultState... states)
    {
        throw new RuntimeException("Not yet implemented");
    }

    public List<BuildResult> findByAfterBuild(long buildId, int maxResults, ResultState... states)
    {
        throw new RuntimeException("Not yet implemented");
    }

    public BuildResult findByLatestBuild(long buildId, ResultState... states)
    {
        throw new RuntimeException("Not yet implemented");
    }
}
