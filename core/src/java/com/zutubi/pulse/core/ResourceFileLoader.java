package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.Property;
import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.core.model.ResourceVersion;

import java.io.InputStream;

/**
 * Utility class to load resource files.
 */
public class ResourceFileLoader
{
    public static FileResourceRepository load(InputStream input) throws BobException
    {
        FileResourceRepository repository = new FileResourceRepository();
        return load(input, repository);
    }

    public static FileResourceRepository load(InputStream input, FileResourceRepository repository) throws BobException
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
