package com.zutubi.pulse.core;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.engine.ProjectRecipesConfiguration;
import com.zutubi.pulse.core.engine.RecipeConfiguration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;

import java.io.ByteArrayInputStream;
import java.util.List;

/**
 * Convenience class for creating loaders for pulse files with types registered.
 */
public class PulseFileLoader extends ToveFileLoader
{
    /**
     * Use the PulseFileLoaderFactory to create a correctly configured instance of
     * the PulseFileLoader.
     */
    public PulseFileLoader()
    {
    }

    /**
     * Retrieves a list of available recipes from a pulse file.
     *
     * @param pulseFile source of the pulse file (an XML string)
     * @return a list of all recipe names in the file
     * @throws PulseException if the file cannot be loaded
     */
    public List<String> loadAvailableRecipes(String pulseFile) throws PulseException
    {
        ProjectRecipesConfiguration recipes = new ProjectRecipesConfiguration();
        RecipeListingPredicate predicate = new RecipeListingPredicate();
        load(new ByteArrayInputStream(pulseFile.getBytes()), recipes, new PulseScope(), predicate);

        return CollectionUtils.map(recipes.getRecipes().values(), new Mapping<RecipeConfiguration, String>()
        {
            public String map(RecipeConfiguration recipe)
            {
                return recipe.getName();
            }
        });
    }
}
