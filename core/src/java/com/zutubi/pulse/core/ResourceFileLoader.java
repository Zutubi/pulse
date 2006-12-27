package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.core.model.ResourceProperty;
import com.zutubi.pulse.core.model.ResourceVersion;

import java.io.InputStream;

/**
 * Utility class to load resource files.
 */
public class ResourceFileLoader
{
    public static FileResourceRepository load(InputStream input) throws PulseException
    {
        FileResourceRepository repository = new FileResourceRepository();
        return load(input, repository);
    }

    public static FileResourceRepository load(InputStream input, FileResourceRepository repository) throws PulseException
    {
        FileLoader loader = createLoader();
        loader.load(input, repository);
        return repository;
    }

    private static FileLoader createLoader()
    {
        FileLoader loader = new FileLoader();
        loader.setObjectFactory(new ObjectFactory());
        ComponentRegistry registry = new ComponentRegistry();
        registry.register("resource", Resource.class);
        registry.register("version", ResourceVersion.class);
        registry.register("property", ResourceProperty.class);
        loader.setRegistry(registry);
        return loader;
    }
}
