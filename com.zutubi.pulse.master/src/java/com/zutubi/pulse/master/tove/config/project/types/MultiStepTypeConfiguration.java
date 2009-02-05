package com.zutubi.pulse.master.tove.config.project.types;

import com.zutubi.pulse.core.PulseFileLoaderFactory;
import com.zutubi.pulse.core.ToveFileStorer;
import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.pulse.core.engine.ProjectRecipesConfiguration;
import com.zutubi.pulse.core.engine.RecipeConfiguration;
import com.zutubi.pulse.core.personal.PatchArchive;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.tove.annotations.Ordered;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wire;
import com.zutubi.util.io.IOUtils;
import nu.xom.Element;

import java.io.ByteArrayOutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 */
@SymbolicName("zutubi.multiStepTypeConfig")
@Wire
public class MultiStepTypeConfiguration extends TypeConfiguration
{
    @Ordered
    private Map<String, CommandConfiguration> commands = new LinkedHashMap<String, CommandConfiguration>();
    private PulseFileLoaderFactory fileLoaderFactory;

    public String getPulseFile(ProjectConfiguration projectConfig, Revision revision, PatchArchive patch) throws Exception
    {
        RecipeConfiguration recipe = new RecipeConfiguration();
        recipe.setName("default");
        recipe.getCommands().putAll(commands);

        ProjectRecipesConfiguration recipes = new ProjectRecipesConfiguration();
        recipes.getRecipes().put(recipe.getName(), recipe);
        recipes.setDefaultRecipe(recipe.getName());

        ToveFileStorer fileStorer = fileLoaderFactory.createStorer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try
        {
            fileStorer.store(baos, recipes, new Element("project"));
            return new String(baos.toByteArray());
        }
        finally
        {
            IOUtils.close(baos);
        }
    }

    public Map<String, CommandConfiguration> getCommands()
    {
        return commands;
    }

    public void setCommands(Map<String, CommandConfiguration> commands)
    {
        this.commands = commands;
    }

    public void setFileLoaderFactory(PulseFileLoaderFactory fileLoaderFactory)
    {
        this.fileLoaderFactory = fileLoaderFactory;
    }
}
