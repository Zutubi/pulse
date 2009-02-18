package com.zutubi.pulse.core.commands.api;

import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.util.bean.ObjectFactory;

/**
 */
@SymbolicName("zutubi.outputConfigSupport")
public abstract class OutputConfigurationSupport extends AbstractNamedConfiguration implements OutputConfiguration
{
    private ObjectFactory objectFactory;

    public <T extends Output> T buildOutput(Class<T> type)
    {
        try
        {
            return objectFactory.buildBean(type, new Class[]{getClass()}, new Object[]{this});
        }
        catch (Exception e)
        {
            throw new BuildException("Unable to instantiate output '" + getName() + "': " + e.getMessage(), e);
        }
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
