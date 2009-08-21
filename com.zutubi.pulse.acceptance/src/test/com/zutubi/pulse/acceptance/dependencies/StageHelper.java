package com.zutubi.pulse.acceptance.dependencies;

import java.util.HashMap;
import java.util.Map;

public class StageHelper
{
    private ProjectHelper project;
    private String name;
    private RecipeHelper recipe;

    private Map<String, String> properties = new HashMap<String, String>();

    public StageHelper(ProjectHelper project, String name)
    {
        this.setName(name);
        this.setProject(project);
    }

    public void addProperty(String name, String value)
    {
        properties.put(name, value);
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public ProjectHelper getProject()
    {
        return project;
    }

    public void setProject(ProjectHelper project)
    {
        this.project = project;
    }

    public RecipeHelper getRecipe()
    {
        if (recipe != null)
        {
            return recipe;
        }
        return project.getDefaultRecipe();
    }

    public void setRecipe(RecipeHelper recipe)
    {
        this.recipe = recipe;
    }

    public Map<String, String> getProperties()
    {
        return properties;
    }
}
