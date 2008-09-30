package com.zutubi.pulse.core;

import nu.xom.Element;

import java.util.LinkedList;
import java.util.List;

/**
 */
public class ResourceRequirementsPredicate implements TypeLoadPredicate
{
    private PulseFile pulseFile;
    private String recipeName;
    private List<ResourceReference> references;

    public ResourceRequirementsPredicate(PulseFile pulseFile, String recipeName)
    {
        this.pulseFile = pulseFile;
        this.recipeName = recipeName;
        references = new LinkedList<ResourceReference>();
    }

    public boolean loadType(Object type, Element element)
    {
        if(type instanceof Recipe)
        {
            if(recipeName == null)
            {
                recipeName = pulseFile.getDefaultRecipe();
            }

            if(recipeName == null)
            {
                return false;
            }
            else
            {
                Recipe recipe = (Recipe) type;
                return recipe.getName().equals(recipeName);
            }
        }
        else if(type instanceof ResourceReference)
        {
            references.add((ResourceReference) type);
        }
        else
        {
            return true;
        }

        return false;
    }

    public boolean resolveReferences(Object type, Element element)
    {
        return type instanceof ResourceReference;
    }

    public boolean allowUnresolved(Object type, Element element)
    {
        return false;
    }

    public boolean validate(Object type, Element element)
    {
        return type instanceof ResourceReference;
    }

    public List<ResourceReference> getReferences()
    {
        return references;
    }
}
