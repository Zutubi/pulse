package com.zutubi.pulse.master.tove.config.project.types;

import com.zutubi.pulse.core.engine.FixedPulseFileSource;
import com.zutubi.pulse.core.engine.ProjectRecipesConfiguration;
import com.zutubi.pulse.core.engine.PulseFileSource;
import com.zutubi.pulse.core.engine.RecipeConfiguration;
import com.zutubi.pulse.core.engine.marshal.PulseFileLoaderFactory;
import com.zutubi.pulse.core.marshal.ToveFileStorer;
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
    @Wizard.Ignore
    private String defaultRecipe;
    private Map<String, RecipeConfiguration> recipes = new LinkedHashMap<String, RecipeConfiguration>();
    @Transient
    private PulseFileLoaderFactory fileLoaderFactory;

    public PulseFileSource getPulseFile() throws Exception
    {
        ProjectRecipesConfiguration prc = new ProjectRecipesConfiguration();
        prc.setDefaultRecipe(defaultRecipe);
        prc.getRecipes().putAll(recipes);

        ToveFileStorer fileStorer = fileLoaderFactory.createStorer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try
        {
            fileStorer.store(baos, prc, new Element("project"));
            return new FixedPulseFileSource(new String(baos.toByteArray()));
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
