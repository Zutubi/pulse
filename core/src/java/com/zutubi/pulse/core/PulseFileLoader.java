package com.zutubi.pulse.core;

import com.zutubi.pulse.model.ResourceRequirement;

import java.io.ByteArrayInputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Convenience class for creating loaders for pulse files with types registered.
 */
public class PulseFileLoader extends FileLoader
{
    /**
     * Package protected: use PulseFileLoaderFactory to create.
     */
    PulseFileLoader()
    {

        register("register", Register.class);
        register("version", Version.class);
    }

    public List<ResourceRequirement> loadRequiredResources(String pulseFile, String recipe) throws PulseException
    {
        List<ResourceRequirement> requirements = new LinkedList<ResourceRequirement>();

        PulseFile file = new PulseFile();
        ResourceRequirementsPredicate predicate = new ResourceRequirementsPredicate(file, recipe);
        load(new ByteArrayInputStream(pulseFile.getBytes()), file, new Scope(), new FileResourceRepository(), predicate);

        for(ResourceReference reference: predicate.getReferences())
        {
            if(reference.isRequired())
            {
                requirements.add(new ResourceRequirement(reference.getName(), reference.getVersion()));
            }
        }

        return requirements;
    }
}
