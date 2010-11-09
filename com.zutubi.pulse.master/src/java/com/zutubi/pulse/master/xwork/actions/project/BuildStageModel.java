package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.tove.model.ActionLink;
import com.zutubi.pulse.master.webwork.Urls;

import java.util.List;

/**
 * Defines JSON data for a build stage.
 */
public class BuildStageModel extends ResultModel
{
    private String name;
    private String recipe;
    private String agent;
    private String tests;
    private String buildLink;
    private List<ActionLink> featuredArtifacts;

    public BuildStageModel(BuildResult buildResult, RecipeResultNode stageResult)
    {
        super(stageResult.getResult());
        name = stageResult.getStageName();
        RecipeResult recipeResult = stageResult.getResult();
        recipe = recipeResult.getRecipeName();
        agent = stageResult.getHost();
        tests = recipeResult.getTestSummary().toString();
        buildLink = Urls.getBaselessInstance().build(buildResult).substring(1);
    }

    public String getName()
    {
        return name;
    }

    public String getRecipe()
    {
        return recipe;
    }

    public String getAgent()
    {
        return agent;
    }

    public String getTests()
    {
        return tests;
    }

    public String getBuildLink()
    {
        return buildLink;
    }

    public List<ActionLink> getFeaturedArtifacts()
    {
        return featuredArtifacts;
    }

    public void setFeaturedArtifacts(List<ActionLink> featuredArtifacts)
    {
        this.featuredArtifacts = featuredArtifacts;
    }
}
