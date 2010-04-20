package com.zutubi.pulse.core;

import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.pulse.core.engine.ProjectRecipesConfiguration;
import com.zutubi.pulse.core.engine.RecipeConfiguration;
import com.zutubi.pulse.core.engine.api.BuildProperties;
import com.zutubi.pulse.core.engine.api.Scope;
import com.zutubi.pulse.core.marshal.ToveFileLoadInterceptor;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.util.StringUtils;
import nu.xom.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * An interceptor to ensure only the recipe of the given name is loaded.  This
 * interceptor also collects scope information for all commands in the recipe.
 */
public class RecipeLoadInterceptor implements ToveFileLoadInterceptor
{
    private ProjectRecipesConfiguration recipesConfiguration;
    private String recipeName;
    private Map<String, Scope> commandScopes = new HashMap<String, Scope>();
    
    public RecipeLoadInterceptor(ProjectRecipesConfiguration recipesConfiguration, String recipeName)
    {
        this.recipesConfiguration = recipesConfiguration;
        this.recipeName = recipeName;
    }

    public boolean loadInstance(Configuration instance, Element element, Scope scope)
    {
        boolean load = acceptInstance(instance);
        if (load && instance instanceof CommandConfiguration)
        {
            commandScopes.put(((CommandConfiguration)instance).getName(), scope.copyTo(scope.getAncestor(BuildProperties.SCOPE_RECIPE)));
        }

        return load;
    }

    public boolean allowUnresolved(Configuration instance, Element element)
    {
        return false;
    }

    public boolean validate(Configuration instance, Element element)
    {
        return acceptInstance(instance);
    }

    /**
     * Returns the scope associated with the given command, if any.  This stores
     * only properties defined within the Pulse file up to where the command
     * appears.
     * 
     * @param commandName name of the command to retrieve the scope for
     * @return the scope for the given command, or null if no such scope exists
     */
    public Scope getCommandScope(String commandName)
    {
        return commandScopes.get(commandName);
    }
    
    private boolean acceptInstance(Configuration instance)
    {
        if (instance instanceof RecipeConfiguration)
        {
            if (!StringUtils.stringSet(recipeName))
            {
                recipeName = recipesConfiguration.getDefaultRecipe();
            }

            if (!StringUtils.stringSet(recipeName))
            {
                return false;
            }
            else
            {
                RecipeConfiguration recipe = (RecipeConfiguration) instance;
                return recipe.getName().equals(recipeName);
            }
        }

        return true;
    }
}
