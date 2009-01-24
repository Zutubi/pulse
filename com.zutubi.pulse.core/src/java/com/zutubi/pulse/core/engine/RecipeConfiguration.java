package com.zutubi.pulse.core.engine;

import com.zutubi.pulse.core.Recipe;
import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.tove.annotations.Ordered;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.util.bean.ObjectFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 */
@SymbolicName("zutubi.recipeConfig")
public class RecipeConfiguration extends AbstractNamedConfiguration
{
    @Ordered
    private Map<String, CommandConfiguration> commands = new LinkedHashMap<String, CommandConfiguration>();
    private ObjectFactory objectFactory;

    public Map<String, CommandConfiguration> getCommands()
    {
        return commands;
    }

    public void setCommands(Map<String, CommandConfiguration> commands)
    {
        this.commands = commands;
    }

    public Recipe createRecipe()
    {
        try
        {
            return objectFactory.buildBean(Recipe.class, new Class[]{ RecipeConfiguration.class }, new Object[]{this});
        }
        catch (Exception e)
        {
            throw new BuildException(e);
        }
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
