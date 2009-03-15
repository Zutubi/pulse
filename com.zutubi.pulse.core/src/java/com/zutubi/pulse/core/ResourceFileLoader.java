package com.zutubi.pulse.core;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.config.ResourceConfiguration;
import com.zutubi.pulse.core.marshal.LocalFileResolver;
import com.zutubi.pulse.core.marshal.ToveFileLoader;
import com.zutubi.pulse.core.marshal.TypeDefinitions;
import com.zutubi.pulse.core.validation.PulseValidationManager;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.util.bean.ObjectFactory;

import java.io.File;
import java.io.IOException;

/**
 * Utility class to load resource files.
 */
public class ResourceFileLoader
{
    private TypeRegistry typeRegistry;
    private ObjectFactory objectFactory;

    public InMemoryResourceRepository load(File input) throws PulseException, IOException
    {
        InMemoryResourceRepository repository = new InMemoryResourceRepository();
        return load(input, repository);
    }

    public InMemoryResourceRepository load(File input, InMemoryResourceRepository repository) throws PulseException, IOException
    {
        ToveFileLoader loader = createLoader();
        ResourcesConfiguration configuration = new ResourcesConfiguration();
        loader.load(input, configuration, new LocalFileResolver(input.getParentFile()));
        for (ResourceConfiguration resource: configuration.getResources().values())
        {
            repository.addResource(resource);
        }
        return repository;
    }

    private ToveFileLoader createLoader()
    {
        TypeDefinitions definitions = new TypeDefinitions();
        definitions.register("resource", typeRegistry.getType(ResourceConfiguration.class));

        ToveFileLoader loader = new ToveFileLoader();
        loader.setObjectFactory(objectFactory);
        loader.setValidationManager(new PulseValidationManager());
        loader.setTypeRegistry(typeRegistry);
        loader.setTypeDefinitions(definitions);
        return loader;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
