package com.zutubi.pulse.master.tove.config.project.types;

import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.pulse.core.engine.ProjectRecipesConfiguration;
import com.zutubi.pulse.core.engine.RecipeConfiguration;
import com.zutubi.pulse.core.personal.PatchArchive;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.tove.annotations.Ordered;
import com.zutubi.tove.annotations.SymbolicName;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 */
@SymbolicName("zutubi.multiStepTypeConfig")
public class MultiStepTypeConfiguration extends TypeConfiguration
{
    @Ordered
    private Map<String, CommandConfiguration> commands = new LinkedHashMap<String, CommandConfiguration>();

    public String getPulseFile(ProjectConfiguration projectConfig, Revision revision, PatchArchive patch) throws Exception
    {
        RecipeConfiguration recipe = new RecipeConfiguration();
        recipe.setName("default");
        recipe.getCommands().putAll(commands);

        ProjectRecipesConfiguration recipes = new ProjectRecipesConfiguration();
        recipes.getRecipes().put(recipe.getName(), recipe);
        recipes.setDefaultRecipe(recipe.getName());

        throw new RuntimeException("Not implemented");
    }

    public Map<String, CommandConfiguration> getCommands()
    {
        return commands;
    }

    public void setCommands(Map<String, CommandConfiguration> commands)
    {
        this.commands = commands;
    }
}
