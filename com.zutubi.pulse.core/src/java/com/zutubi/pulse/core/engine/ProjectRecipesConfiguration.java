package com.zutubi.pulse.core.engine;

import com.zutubi.pulse.core.engine.api.PropertyConfiguration;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 */
@SymbolicName("zutubi.projectRecipes")
public class ProjectRecipesConfiguration extends AbstractConfiguration
{
    private String defaultRecipe;
    private Map<String, RecipeConfiguration> recipes = new LinkedHashMap<String, RecipeConfiguration>();
    private Map<String, PostProcessorConfiguration> postProcessors = new LinkedHashMap<String, PostProcessorConfiguration>();
    private Map<String, PropertyConfiguration> properties = new HashMap<String, PropertyConfiguration>();

    public String getDefaultRecipe()
    {
        return defaultRecipe;
    }

    public void setDefaultRecipe(String defaultRecipe)
    {
        this.defaultRecipe = defaultRecipe;
    }

    public Map<String, RecipeConfiguration> getRecipes()
    {
        return recipes;
    }

    public void setRecipes(Map<String, RecipeConfiguration> recipes)
    {
        this.recipes = recipes;
    }

    public void addRecipe(RecipeConfiguration recipe)
    {
        recipes.put(recipe.getName(), recipe);
    }
    
    public Map<String, PostProcessorConfiguration> getPostProcessors()
    {
        return postProcessors;
    }

    public void setPostProcessors(Map<String, PostProcessorConfiguration> postProcessors)
    {
        this.postProcessors = postProcessors;
    }

    public void addPostProcessor(PostProcessorConfiguration postProcessor)
    {
        postProcessors.put(postProcessor.getName(), postProcessor);
    }

    public Map<String, PropertyConfiguration> getProperties()
    {
        return properties;
    }

    public void setProperties(Map<String, PropertyConfiguration> properties)
    {
        this.properties = properties;
    }

    public void addProperty(PropertyConfiguration property)
    {
        properties.put(property.getName(), property);
    }
}
