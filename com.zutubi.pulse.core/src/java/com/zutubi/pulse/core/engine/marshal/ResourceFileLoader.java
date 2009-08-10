package com.zutubi.pulse.core.engine.marshal;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.config.ResourceConfiguration;
import com.zutubi.pulse.core.engine.ResourcesConfiguration;
import com.zutubi.pulse.core.engine.SimpleResourceRequirementConfiguration;
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
    public static final String ROOT_ELEMENT = "resources";

    private static final String ELEMENT_RESOURCE = "resource";
    private static final String ELEMENT_REQUIRE = "require";

    protected TypeDefinitions typeDefinitions;

    protected ToveFileLoader loader;
    protected TypeRegistry typeRegistry;
    protected ObjectFactory objectFactory;

    public void init()
    {
        typeDefinitions = new TypeDefinitions();
        typeDefinitions.register(ELEMENT_REQUIRE, typeRegistry.getType(SimpleResourceRequirementConfiguration.class));
        typeDefinitions.register(ELEMENT_RESOURCE, typeRegistry.getType(ResourceConfiguration.class));

        loader = new ToveFileLoader();
        loader.setObjectFactory(objectFactory);
        loader.setValidationManager(new PulseValidationManager());
        loader.setTypeRegistry(typeRegistry);
        loader.setTypeDefinitions(typeDefinitions);
    }

    public ResourcesConfiguration load(File input) throws PulseException, IOException
    {
        ResourcesConfiguration configuration = new ResourcesConfiguration();
        loader.load(input, configuration, new LocalFileResolver(input.getParentFile()));
        return configuration;
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
