package com.cinnamonbob.core;

import com.cinnamonbob.core.model.Property;
import com.cinnamonbob.core.model.Resource;
import com.cinnamonbob.core.model.ResourceVersion;

import java.io.InputStream;

/**
 * Utility class to load resource files.
 */
public class ResourceFileLoader
{
    public static ResourceRepository load(InputStream input) throws BobException
    {
        ResourceRepository repository = new ResourceRepository();
        return load(input, repository);
    }

    public static ResourceRepository load(InputStream input, ResourceRepository repository) throws BobException
    {
        FileLoader loader = createLoader();
        loader.load(input, repository);
        return repository;
    }

    private static FileLoader createLoader()
    {
        FileLoader loader = new FileLoader();
        loader.setObjectFactory(new ObjectFactory());
        loader.register("resource", Resource.class);
        loader.register("version", ResourceVersion.class);
        loader.register("property", Property.class);
        return loader;
    }
}
