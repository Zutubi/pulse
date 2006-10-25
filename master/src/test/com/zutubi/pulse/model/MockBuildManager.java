package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

    public List<BuildResult> queryBuilds(Project[] projects, ResultState[] states, String[] specs, long earliestStartTime, long latestStartTime, Boolean hasWorkDir, int first, int max, boolean mostRecentFirst)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public List<BuildResult> querySpecificationBuilds(Project project, String spec, ResultState[] states, long lowestNumber, long highestNumber, int first, int max, boolean mostRecentFirst, boolean initialise)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public List<BuildResult> getLatestBuildResultsForProject(Project project, int max)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public int getBuildCount(Project project, ResultState[] states, String spec)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public void fillHistoryPage(HistoryPage page)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public void fillHistoryPage(HistoryPage page, ResultState[] states, String spec)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public List<String> getBuildSpecifications(Project project)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public List<BuildResult> getLatestCompletedBuildResults(Project project, String spec, int max)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public List<BuildResult> getLatestCompletedBuildResults(Project project, String spec, int first, int max)
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

    public BuildResult getByProjectAndNumber(final Project project, final long number)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public long getNextBuildNumber(Project project)
    {
        return nextBuildNumber++;
    }

    public void cleanupBuilds()
    {
        throw new RuntimeException("Method not implemented.");
    }

    public Revision getPreviousRevision(Project project, String specification)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public List<Changelist> getLatestChangesForUser(User user, int max)
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
            if(c.getResultIds().contains(result.getId()))
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

    public void abortUnfinishedBuilds(Project project, String message)
    {
        BuildResult result = getLatestBuildResult(project);
        if(result != null && result.inProgress())
        {
            result.error(message);
            result.complete();
        }
    }

    public void abortUnfinishedBuilds(User user, String message)
    {
        BuildResult result = null;

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

    public void cleanupBuilds(User user)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public boolean canCancel(BuildResult build, User user)
    {
        throw new RuntimeException("Method not implemented.");
    }
}