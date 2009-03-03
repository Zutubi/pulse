package com.zutubi.pulse.core;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.config.ResourceConfiguration;
import com.zutubi.pulse.core.validation.PulseValidationManager;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.util.bean.DefaultObjectFactory;

import java.io.InputStream;

/**
 * Utility class to load resource files.
 */
public class ResourceFileLoader
{
    private TypeRegistry typeRegistry;

    public InMemoryResourceRepository load(InputStream input) throws PulseException
    {
        InMemoryResourceRepository repository = new InMemoryResourceRepository();
        return load(input, repository);
    }

    public InMemoryResourceRepository load(InputStream input, InMemoryResourceRepository repository) throws PulseException
    {
        ToveFileLoader loader = createLoader();
        ResourcesConfiguration configuration = new ResourcesConfiguration();
        loader.load(input, configuration);
        for (ResourceConfiguration resource: configuration.getResources().values())
        {
            repository.addResource(resource);
        }
        return repository;
    }

    private ToveFileLoader createLoader()
    {
        ToveFileLoader loader = new ToveFileLoader();
        loader.setObjectFactory(new DefaultObjectFactory());
        loader.setValidationManager(new PulseValidationManager());
        loader.setTypeRegistry(typeRegistry);
        loader.register("resource", typeRegistry.getType(ResourceConfiguration.class));
        return loader;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}
