package com.zutubi.pulse.master.tove.config.project.types;

import com.zutubi.pulse.core.PulseFileLoaderFactory;
import com.zutubi.pulse.core.ToveFileStorer;
import com.zutubi.pulse.core.engine.ProjectRecipesConfiguration;
import com.zutubi.pulse.core.engine.RecipeConfiguration;
import com.zutubi.pulse.core.personal.PatchArchive;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.tove.annotations.*;
import com.zutubi.util.io.IOUtils;
import nu.xom.Element;

import java.io.ByteArrayOutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A project type that allows multiple recipes to be fully configured in the
 * UI.
 */
@SymbolicName("zutubi.multiRecipeTypeConfig")
@Wire
@Form(fieldOrder = {"defaultRecipe"})
public class MultiRecipeTypeConfiguration extends TypeConfiguration
{
    @Select(optionProvider = "MultiRecipeTypeDefaultRecipeOptionProvider", editable = true)
    private String defaultRecipe;
    private Map<String, RecipeConfiguration> recipes = new LinkedHashMap<String, RecipeConfiguration>();
    @Transient
    private PulseFileLoaderFactory fileLoaderFactory;

    public String getPulseFile(ProjectConfiguration projectConfig, Revision revision, PatchArchive patch) throws Exception
    {
        ProjectRecipesConfiguration prc = new ProjectRecipesConfiguration();
        prc.setDefaultRecipe(defaultRecipe);
        prc.getRecipes().putAll(recipes);

        ToveFileStorer fileStorer = fileLoaderFactory.createStorer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try
        {
            fileStorer.store(baos, prc, new Element("project"));
            return new String(baos.toByteArray());
        }
        finally
        {
            IOUtils.close(baos);
        }
    }

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
    
    public void setFileLoaderFactory(PulseFileLoaderFactory fileLoaderFactory)
    {
        this.fileLoaderFactory = fileLoaderFactory;
    }
}
