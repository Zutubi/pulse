package com.cinnamonbob.model;

import com.cinnamonbob.core.model.*;

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

    public void clear()
    {
        buildResults.clear();
        recipeResultNodes.clear();
        recipeResults.clear();
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

    public BuildResult getPreviousBuildResult(BuildResult result)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public BuildResult getLatestBuildResult(Project project)
    {
        BuildResult result = null;

        for (BuildResult r : buildResults.values())
        {
            if (r.getProject().equals(project))
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

    public List<Changelist> getLatestChangesForUser(User user, int max)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public List<Changelist> getLatestChangesForProject(Project project, int max)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public void deleteAllBuilds(Project project)
    {
        throw new RuntimeException("Method not implemented.");
    }
}