package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.*;

import java.util.*;

public class MockBuildManager implements BuildManager
{
    private long nextId = 1;
    private long nextBuildNumber = 1;
    private Map<Long, BuildResult> buildResults = new TreeMap<Long, BuildResult>();
    private Map<Long, RecipeResultNode> recipeResultNodes = new TreeMap<Long, RecipeResultNode>();
    private Map<Long, RecipeResult> recipeResults = new TreeMap<Long, RecipeResult>();
    private Map<Long, Changelist> changelists = new TreeMap<Long, Changelist>();

    public void clear()
    {
        buildResults.clear();
        recipeResultNodes.clear();
        recipeResults.clear();
        changelists.clear();
    }

    public void save(BuildResult result)
    {
        if (result.getId() == 0)
        {
            result.setId(nextId++);
        }
        buildResults.put(result.getId(), result);
    }

    public void save(RecipeResultNode node)
    {
        recipeResultNodes.put(node.getId(), node);
    }

    public void save(RecipeResult result)
    {
        recipeResults.put(result.getId(), result);
    }

    public void save(Changelist changelist)
    {
        changelists.put(changelist.getId(), changelist);
    }

    public BuildResult getBuildResult(long id)
    {
        return buildResults.get(id);
    }

    public RecipeResultNode getRecipeResultNode(long id)
    {
        return recipeResultNodes.get(id);
    }

    public RecipeResult getRecipeResult(long id)
    {
        return recipeResults.get(id);
    }

    public CommandResult getCommandResult(long id)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public StoredArtifact getArtifact(long id)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public RecipeResultNode getResultNodeByResultId(long id)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public StoredFileArtifact getFileArtifact(long id)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public List<BuildResult> getPersonalBuilds(User user)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public BuildResult getLatestBuildResult(User user)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public BuildResult getLatestSuccessfulBuildResult(Project project)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public BuildResult getLatestSuccessfulBuildResult()
    {
        throw new RuntimeException("Method not implemented.");
    }

    public List<BuildResult> queryBuilds(Project[] projects, ResultState[] states, long earliestStartTime, long latestStartTime, Boolean hasWorkDir, int first, int max, boolean mostRecentFirst)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public List<BuildResult> queryBuilds(Project project, ResultState[] states, long lowestNumber, long highestNumber, int first, int max, boolean mostRecentFirst, boolean initialise)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public List<BuildResult> getLatestBuildResultsForProject(Project project, int max)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public int getBuildCount(Project project, ResultState[] states)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public int getBuildCount(Project project, long after, long upTo)
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public void fillHistoryPage(HistoryPage page)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public void fillHistoryPage(HistoryPage page, ResultState[] states)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public List<BuildResult> getLatestCompletedBuildResults(Project project, int max)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public List<BuildResult> getLatestCompletedBuildResults(Project project, int first, int max)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public BuildResult getPreviousBuildResult(BuildResult result)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public BuildResult getLatestBuildResult(Project project)
    {
        BuildResult result = null;

        for (BuildResult r : buildResults.values())
        {
            if (!r.isPersonal() && r.getProject().equals(project))
            {
                if (result == null || result.getNumber() < r.getNumber())
                {
                    result = r;
                }
            }
        }

        return result;
    }

    public BuildResult getLatestBuildResult()
    {
        throw new RuntimeException("Method not implemented.");
    }

    public BuildResult getByProjectAndNumber(final Project project, final long number)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public BuildResult getByProjectAndVirtualId(Project project, String buildId)
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public BuildResult getByUserAndNumber(User user, long id)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public long getNextBuildNumber(Project project)
    {
        return nextBuildNumber++;
    }

    public Revision getPreviousRevision(Project project)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public List<Changelist> getLatestChangesForUser(User user, int max)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public StoredArtifact getArtifact(long buildId, String artifactName)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public StoredArtifact getCommandResultByArtifact(long commandResultId, String artifactName)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public Boolean canDecorateArtifact(long artifactId)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public List<Changelist> getLatestChangesForProject(Project project, int max)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public List<Changelist> getLatestChangesForProjects(Project[] projects, int max)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public List<Changelist> getChangesForBuild(BuildResult result)
    {
        List<Changelist> lists = new LinkedList<Changelist>();
        for(Changelist c: changelists.values())
        {
            if(c.getResultId() == result.getId())
            {
                lists.add(c);
            }
        }
        return lists;
    }

    public void deleteAllBuilds(Project project)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public void deleteAllBuilds(User user)
    {
    }

    public Changelist getChangelistByRevision(String serverUid, Revision revision)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public void delete(BuildResult result)
    {
        buildResults.remove(result.getId());
    }

    @SuppressWarnings({ "unchecked" })
    public List<BuildResult> abortUnfinishedBuilds(Project project, String message)
    {
        BuildResult result = getLatestBuildResult(project);
        if(result != null && result.inProgress())
        {
            result.error(message);
            result.complete();
            return Arrays.asList(result);
        }

        return Collections.EMPTY_LIST;
    }

    public void abortUnfinishedBuilds(User user, String message)
    {
        for (BuildResult r : buildResults.values())
        {
            if (user.equals(r.getOwner()) && r.inProgress())
            {
                r.error(message);
                r.complete();
            }
        }
    }

    public boolean isSpaceAvailableForBuild()
    {
        return true;
    }

    public CommandResult getCommandResultByArtifact(long artifactId)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public CommandResult getCommandResult(long id, String commandName)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public void cleanupResult(BuildResult build, boolean rmdir)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public void cleanupWork(BuildResult build)
    {
        throw new RuntimeException("Method not implemented.");
    }

    // debugging hack: need to work out a better way
    public void executeInTransaction(Runnable runnable)
    {
        runnable.run();
    }
}