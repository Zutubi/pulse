package com.zutubi.pulse.core;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.core.config.ResourceVersion;
import com.zutubi.pulse.core.engine.api.ResourceProperty;
import com.zutubi.util.bean.DefaultObjectFactory;

import java.io.File;
import java.io.IOException;

/**
 * Utility class to load resource files.
 */
public class ResourceFileLoader
{
    public static FileResourceRepository load(File input) throws PulseException, IOException
    {
        FileResourceRepository repository = new FileResourceRepository();
        return load(input, repository);
    }

    public static FileResourceRepository load(File input, FileResourceRepository repository) throws PulseException, IOException
    {
        FileLoader loader = createLoader();
        loader.load(input, repository, new LocalFileResolver(input.getParentFile()));
        return repository;
    }

    private static FileLoader createLoader()
    {
        FileLoader loader = new FileLoader();
        loader.setObjectFactory(new DefaultObjectFactory());
        loader.register("resource", Resource.class);
        loader.register("require", SimpleResourceRequirement.class);
        loader.register("version", ResourceVersion.class);
        loader.register("property", ResourceProperty.class);
        return loader;
    }
}
