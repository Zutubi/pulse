package com.zutubi.pulse.acceptance.dependencies;

import java.util.HashMap;
import java.util.Map;

public class Stage
{
    private Project project;
    private String name;
    private Recipe recipe;

    private Map<String, String> properties = new HashMap<String, String>();

    public Stage(Project project, String name)
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

    public Project getProject()
    {
        return project;
    }

    public void setProject(Project project)
    {
        this.project = project;
    }

    public Recipe getRecipe()
    {
        if (recipe != null)
        {
            return recipe;
        }
        return project.getDefaultRecipe();
    }

    public void setRecipe(Recipe recipe)
    {
        this.recipe = recipe;
    }

    public Map<String, String> getProperties()
    {
        return properties;
    }
}
