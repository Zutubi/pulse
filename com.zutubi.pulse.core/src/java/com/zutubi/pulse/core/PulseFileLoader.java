package com.zutubi.pulse.core;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.config.ResourceRequirement;
import com.zutubi.pulse.core.engine.ProjectRecipesConfiguration;
import com.zutubi.pulse.core.engine.RecipeConfiguration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.TextUtils;
import com.zutubi.tove.type.TypeRegistry;

import java.io.ByteArrayInputStream;
import java.util.LinkedList;
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

    public List<ResourceRequirement> loadRequiredResources(String pulseFile, String recipe) throws PulseException
    {
        List<ResourceRequirement> requirements = new LinkedList<ResourceRequirement>();

        // FIXME loader
//        PulseFile file = new PulseFile();
//        ResourceRequirementsPredicate predicate = new ResourceRequirementsPredicate(file, recipe);
//        load(new ByteArrayInputStream(pulseFile.getBytes()), file, new PulseScope(), new FileResourceRepository(), predicate);
//
//        for(ResourceReference reference: predicate.getReferences())
//        {
//            if(reference.isRequired())
//            {
//                // if a version is specified, then we want that version, otherwise the default is fine.
//                if (TextUtils.stringSet(reference.getVersion()))
//                {
//                    requirements.add(new ResourceRequirement(reference.getName(), reference.getVersion(), false));
//                }
//                else
//                {
//                    requirements.add(new ResourceRequirement(reference.getName(), reference.getVersion(), true));
//                }
//            }
//        }

        return requirements;
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
