package com.zutubi.pulse.core;

import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.core.config.ResourceVersion;
import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.util.bean.DefaultObjectFactory;

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
        loader.setObjectFactory(new DefaultObjectFactory());
        loader.register("resource", Resource.class);
        loader.register("version", ResourceVersion.class);
        loader.register("property", ResourceProperty.class);
        return loader;
    }
}
