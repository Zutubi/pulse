package com.zutubi.pulse.master.engine.marshal;

import com.zutubi.pulse.core.engine.ReferenceCollectingProjectRecipesConfiguration;
import com.zutubi.pulse.core.engine.marshal.PulseFileLoaderFactory;
import com.zutubi.pulse.core.marshal.doc.ToveFileDocManager;
import com.zutubi.tove.type.CompositeType;

/**
 * Master version of {@link com.zutubi.pulse.core.engine.marshal.PulseFileLoaderFactory},
 * extends the core version by adding generation of documentation.
 */
public class MasterPulseFileLoaderFactory extends PulseFileLoaderFactory
{
    private ToveFileDocManager toveFileDocManager;

    @Override
    public void init()
    {
        super.init();
        toveFileDocManager.registerRoot(ROOT_ELEMENT, typeRegistry.getType(ReferenceCollectingProjectRecipesConfiguration.class), typeDefinitions);
    }

    @Override
    public CompositeType register(String name, Class clazz)
    {
        CompositeType type = super.register(name, clazz);
        toveFileDocManager.registerType(name, type, typeDefinitions);
        return type;
    }

    @Override
    public CompositeType unregister(String name)
    {
        CompositeType type = super.unregister(name);
        if (type != null)
        {
            toveFileDocManager.unregisterType(name, type);
        }

        return type;
    }

    public void setToveFileDocManager(ToveFileDocManager toveFileDocManager)
    {
        this.toveFileDocManager = toveFileDocManager;
    }
}
