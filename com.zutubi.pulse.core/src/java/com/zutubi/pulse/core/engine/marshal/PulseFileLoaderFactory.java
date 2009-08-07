package com.zutubi.pulse.core.engine.marshal;

import com.zutubi.pulse.core.api.PulseRuntimeException;
import com.zutubi.pulse.core.commands.api.DirectoryArtifactConfiguration;
import com.zutubi.pulse.core.commands.api.FileArtifactConfiguration;
import com.zutubi.pulse.core.commands.api.LinkArtifactConfiguration;
import com.zutubi.pulse.core.engine.RecipeConfiguration;
import com.zutubi.pulse.core.engine.api.PropertyConfiguration;
import com.zutubi.pulse.core.marshal.ToveFileStorer;
import com.zutubi.pulse.core.marshal.TypeDefinitions;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.util.bean.ObjectFactory;

/**
 * A factory for creating PulseFileLoader objects that are aware of the
 * current plugins.
 */
public class PulseFileLoaderFactory
{
    public static final String ROOT_ELEMENT = "project";

    protected TypeDefinitions typeDefinitions = new TypeDefinitions();
    protected ObjectFactory objectFactory;
    protected TypeRegistry typeRegistry;

    public void init()
    {
        register("property", PropertyConfiguration.class);
        register("recipe", RecipeConfiguration.class);
        register("dir-artifact", DirectoryArtifactConfiguration.class);
        register("link-artifact", LinkArtifactConfiguration.class);
        register("artifact", FileArtifactConfiguration.class);
    }

    public PulseFileLoader createLoader()
    {
        PulseFileLoader loader = objectFactory.buildBean(PulseFileLoader.class);
        loader.setTypeDefinitions(typeDefinitions);
        return loader;
    }

    public ToveFileStorer createStorer()
    {
        ToveFileStorer storer = objectFactory.buildBean(ToveFileStorer.class);
        storer.setTypeDefinitions(typeDefinitions);
        return storer;
    }

    public TypeDefinitions getTypeDefinitions()
    {
        return typeDefinitions;
    }

    public CompositeType register(String name, Class clazz)
    {
        CompositeType type = typeRegistry.getType(clazz);
        if (type == null)
        {
            throw new PulseRuntimeException("Attempt to register unknown type with file loader: " + clazz.getName());
        }

        typeDefinitions.register(name, type);
        return type;
    }

    public CompositeType unregister(String name)
    {
        return typeDefinitions.unregister(name);
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}
