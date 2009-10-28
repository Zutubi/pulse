package com.zutubi.pulse.core;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.config.ResourceRequirement;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.TextUtils;

import java.io.ByteArrayInputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Convenience class for creating loaders for pulse files with types registered.
 */
public class PulseFileLoader extends FileLoader
{
    /**
     * Use the PulseFileLoaderFactory to create a correctly configured instance of
     * the PulseFileLoader.
     */
    public PulseFileLoader()
    {
    }

    public List<ResourceRequirement> loadRequiredResources(String pulseFile, String recipe, FileResolver fileResolver) throws PulseException
    {
        List<ResourceRequirement> requirements = new LinkedList<ResourceRequirement>();

        if (!Boolean.getBoolean("pulse.ignore.resource.element"))
        {
            PulseFile file = new PulseFile();
            ResourceRequirementsPredicate predicate = new ResourceRequirementsPredicate(file, recipe);
            load(new ByteArrayInputStream(pulseFile.getBytes()), file, new PulseScope(), fileResolver, new FileResourceRepository(), predicate);

            for(ResourceReference reference: predicate.getReferences())
            {
                if(reference.isRequired())
                {
                    // if a version is specified, then we want that version, otherwise the default is fine.
                    if (TextUtils.stringSet(reference.getVersion()))
                    {
                        requirements.add(new ResourceRequirement(reference.getName(), reference.getVersion(), false));
                    }
                    else
                    {
                        requirements.add(new ResourceRequirement(reference.getName(), reference.getVersion(), true));
                    }
                }
            }
        }

        return requirements;
    }

    /**
     * Retrieves a list of available recipes from a pulse file.
     *
     * @param pulseFile source of the pulse file (an XML string)
     * @return a list of all recipe names in the file
     * @throws PulseException if the file cannot be loaded
     */
    public List<String> loadAvailableRecipes(String pulseFile, FileResolver fileResolver) throws PulseException
    {
        PulseFile file = new PulseFile();
        RecipeListingPredicate predicate = new RecipeListingPredicate();
        load(new ByteArrayInputStream(pulseFile.getBytes()), file, new PulseScope(), fileResolver, new FileResourceRepository(), predicate);

        return CollectionUtils.map(file.getRecipes(), new Mapping<Recipe, String>()
        {
            public String map(Recipe recipe)
            {
                return recipe.getName();
            }
        });
    }
}
