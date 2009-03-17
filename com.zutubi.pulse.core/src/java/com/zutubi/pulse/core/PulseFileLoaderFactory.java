package com.zutubi.pulse.core;

import com.zutubi.pulse.core.api.PulseRuntimeException;
import com.zutubi.pulse.core.commands.api.DirectoryOutputConfiguration;
import com.zutubi.pulse.core.commands.api.FileOutputConfiguration;
import com.zutubi.pulse.core.commands.api.LinkOutputConfiguration;
import com.zutubi.pulse.core.engine.ProjectRecipesConfiguration;
import com.zutubi.pulse.core.engine.RecipeConfiguration;
import com.zutubi.pulse.core.engine.api.PropertyConfiguration;
import com.zutubi.pulse.core.marshal.ToveFileStorer;
import com.zutubi.pulse.core.marshal.TypeDefinitions;
import com.zutubi.pulse.core.marshal.doc.ToveFileDocManager;
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

    private TypeDefinitions typeDefinitions = new TypeDefinitions();
    private ObjectFactory objectFactory;
    private TypeRegistry typeRegistry;
    private ToveFileDocManager toveFileDocManager;

    public void init()
    {
        register("property", PropertyConfiguration.class);
        register("recipe", RecipeConfiguration.class);
        register("dir-artifact", DirectoryOutputConfiguration.class);
        register("link-artifact", LinkOutputConfiguration.class);
        register("artifact", FileOutputConfiguration.class);
        register("version", RecipeVersionConfiguration.class);

        if (toveFileDocManager != null)
        {
            toveFileDocManager.registerRoot(ROOT_ELEMENT, typeRegistry.getType(ProjectRecipesConfiguration.class), typeDefinitions);
        }
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

    public void register(String name, Class clazz)
    {
        CompositeType type = typeRegistry.getType(clazz);
        if (type == null)
        {
            throw new PulseRuntimeException("Attempt to register unknown type with file loader: " + clazz.getName());
        }

        typeDefinitions.register(name, type);
        if (toveFileDocManager != null)
        {
            toveFileDocManager.registerType(name, type, typeDefinitions);
        }
    }

    public void unregister(String name)
    {
        CompositeType type = typeDefinitions.unregister(name);
        if (type != null && toveFileDocManager != null)
        {
            toveFileDocManager.unregisterType(name, type);
        }
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setToveFileDocManager(ToveFileDocManager toveFileDocManager)
    {
        this.toveFileDocManager = toveFileDocManager;
    }
}
