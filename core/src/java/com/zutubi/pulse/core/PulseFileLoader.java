package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.Property;
import com.zutubi.pulse.model.ResourceRequirement;
import com.zutubi.pulse.plugins.PluginManager;
import com.zutubi.pulse.api.PulseFileElement;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
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
