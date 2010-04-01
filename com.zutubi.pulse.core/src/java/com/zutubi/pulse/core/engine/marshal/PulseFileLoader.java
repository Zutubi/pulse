package com.zutubi.pulse.core.engine.marshal;

import com.zutubi.pulse.core.PulseScope;
import com.zutubi.pulse.core.RecipeListingPredicate;
import com.zutubi.pulse.core.RecipeLoadPredicate;
import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.engine.ProjectRecipesConfiguration;
import com.zutubi.pulse.core.engine.RecipeConfiguration;
import com.zutubi.pulse.core.marshal.FileResolver;
import com.zutubi.pulse.core.marshal.ToveFileLoader;
import com.zutubi.tove.config.api.ToConfigurationNameMapping;
import com.zutubi.util.CollectionUtils;

import java.io.ByteArrayInputStream;
import java.util.List;

/**
 * Convenience class for common pulse file loading operations.
 */
public class PulseFileLoader extends ToveFileLoader
{
    /**
     * Loads a pulse file, isolating a single recipe.
     *
     * @param pulseFile    source of the pulse file (an XML string)
     * @param recipeName   name of the recipe to load
     * @param globalScope  scope to use as basis for loading
     * @param fileResolver used to resolve any imported files
     * @return configuration for the loaded recipe
     * @throws PulseException if the file cannot be loaded
     */
    public ProjectRecipesConfiguration loadRecipe(String pulseFile, String recipeName, PulseScope globalScope, FileResolver fileResolver) throws PulseException
    {
        ProjectRecipesConfiguration recipes = new ProjectRecipesConfiguration();
        RecipeLoadPredicate predicate = new RecipeLoadPredicate(recipes, recipeName);
        load(new ByteArrayInputStream(pulseFile.getBytes()), recipes, globalScope, fileResolver, predicate);
        return recipes;
    }

    /**
     * Retrieves a list of available recipes from a pulse file.
     *
     * @param pulseFile    source of the pulse file (an XML string)
     * @param fileResolver used to resolve any imported files
     * @return a list of all recipe names in the file
     * @throws PulseException if the file cannot be loaded
     */
    public List<String> loadAvailableRecipes(String pulseFile, FileResolver fileResolver) throws PulseException
    {
        ProjectRecipesConfiguration recipes = new ProjectRecipesConfiguration();
        RecipeListingPredicate predicate = new RecipeListingPredicate();
        load(new ByteArrayInputStream(pulseFile.getBytes()), recipes, new PulseScope(), fileResolver, predicate);

        return CollectionUtils.map(recipes.getRecipes().values(), new ToConfigurationNameMapping<RecipeConfiguration>());
    }
}
