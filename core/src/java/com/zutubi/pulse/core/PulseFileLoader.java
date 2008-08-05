package com.zutubi.pulse.core;

import com.zutubi.pulse.model.ResourceRequirement;
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
        load(new ByteArrayInputStream(pulseFile.getBytes()), file, new PulseScope(), new FileResourceRepository(), predicate);

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

        return requirements;
    }
}
