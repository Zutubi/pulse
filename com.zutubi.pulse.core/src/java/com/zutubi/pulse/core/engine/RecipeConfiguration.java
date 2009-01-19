package com.zutubi.pulse.core.engine;

import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.pulse.core.Recipe;
import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.util.bean.ObjectFactory;

import java.util.Map;
import java.util.HashMap;

/**
 */
@SymbolicName("zutubi.recipeConfig")
public class RecipeConfiguration extends AbstractNamedConfiguration
{
    private Map<String, AbstractCommandConfiguration> commands = new HashMap<String, AbstractCommandConfiguration>();
    private ObjectFactory objectFactory;

    public Map<String, AbstractCommandConfiguration> getCommands()
    {
        return commands;
    }

    public void setCommands(Map<String, AbstractCommandConfiguration> commands)
    {
        this.commands = commands;
    }

    public Recipe createRecipe()
    {
        Recipe recipe = null;
        try
        {
            recipe = objectFactory.buildBean(Recipe.class);
        }
        catch (Exception e)
        {
            throw new BuildException(e);
        }
        
        recipe.setName(getName());
        for (CommandConfiguration commandConfiguration: commands.values())
        {
            recipe.add(commandConfiguration.createCommand(), null);
        }

        return recipe;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
