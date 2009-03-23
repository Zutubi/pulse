package com.zutubi.pulse.master.engine.marshal;

import com.zutubi.pulse.core.engine.ResourcesConfiguration;
import com.zutubi.pulse.core.engine.marshal.ResourceFileLoader;
import com.zutubi.pulse.core.marshal.doc.ToveFileDocManager;

/**
 * Master version of {@link com.zutubi.pulse.core.engine.marshal.ResourceFileLoader},
 * extends the core version by adding generation of documentation.
 */
public class MasterResourceFileLoader extends ResourceFileLoader
{
    private ToveFileDocManager toveFileDocManager;

    @Override
    public void init()
    {
        super.init();
        toveFileDocManager.registerRoot(ROOT_ELEMENT, typeRegistry.getType(ResourcesConfiguration.class), typeDefinitions);
    }

    public void setToveFileDocManager(ToveFileDocManager toveFileDocManager)
    {
        this.toveFileDocManager = toveFileDocManager;
    }
}
