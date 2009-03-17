package com.zutubi.pulse.core;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.config.ResourceConfiguration;
import com.zutubi.pulse.core.marshal.LocalFileResolver;
import com.zutubi.pulse.core.marshal.ToveFileLoader;
import com.zutubi.pulse.core.marshal.TypeDefinitions;
import com.zutubi.pulse.core.marshal.doc.ToveFileDocManager;
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
    public static final String ROOT_ELEMENT = "resources";

    private static final String ELEMENT_RESOURCE = "resource";

    private ToveFileLoader loader;
    private TypeRegistry typeRegistry;
    private ObjectFactory objectFactory;
    private ToveFileDocManager toveFileDocManager;

    public void init()
    {
        TypeDefinitions definitions = new TypeDefinitions();
        definitions.register(ELEMENT_RESOURCE, typeRegistry.getType(ResourceConfiguration.class));

        loader = new ToveFileLoader();
        loader.setObjectFactory(objectFactory);
        loader.setValidationManager(new PulseValidationManager());
        loader.setTypeRegistry(typeRegistry);
        loader.setTypeDefinitions(definitions);

        if(toveFileDocManager != null)
        {
            toveFileDocManager.registerRoot(ROOT_ELEMENT, typeRegistry.getType(ResourcesConfiguration.class), definitions);
        }
    }

    public InMemoryResourceRepository load(File input) throws PulseException, IOException
    {
        return load(input, new InMemoryResourceRepository());
    }

    public InMemoryResourceRepository load(File input, InMemoryResourceRepository repository) throws PulseException, IOException
    {
        ResourcesConfiguration configuration = new ResourcesConfiguration();
        loader.load(input, configuration, new LocalFileResolver(input.getParentFile()));
        for (ResourceConfiguration resource: configuration.getResources().values())
        {
            repository.addResource(resource);
        }
        return repository;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setToveFileDocManager(ToveFileDocManager toveFileDocManager)
    {
        this.toveFileDocManager = toveFileDocManager;
    }
}
