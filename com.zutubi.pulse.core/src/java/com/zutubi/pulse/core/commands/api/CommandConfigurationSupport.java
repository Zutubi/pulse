package com.zutubi.pulse.core.commands.api;

import com.zutubi.pulse.core.Command;
import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Transient;
import com.zutubi.tove.annotations.Wizard;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.util.bean.ObjectFactory;

import java.util.List;

/**
 * Abstract base class to support implementation of the {@link com.zutubi.pulse.core.commands.api.CommandConfiguration}
 * interface.  Provides simple implementations of common properties.
 */
@SymbolicName("zutubi.commandConfigSupport")
public abstract class CommandConfigurationSupport extends AbstractNamedConfiguration implements CommandConfiguration
{
    @Wizard.Ignore
    private boolean force = false;
    private List<OutputConfiguration> outputs;

    @Transient
    private ObjectFactory objectFactory;

    protected <T extends Command> T buildCommand(Class<T> commandType)
    {
        try
        {
            return objectFactory.buildBean(commandType, new Class[]{getClass()}, new Object[]{this});
        }
        catch (Exception e)
        {
            throw new BuildException("Unable to instantiate command '" + getName() + "': " + e.getMessage(), e);
        }
    }
    
    public boolean isForce()
    {
        return force;
    }

    public void setForce(boolean force)
    {
        this.force = force;
    }

    public List<OutputConfiguration> getOutputs()
    {
        return outputs;
    }

    public void setOutputs(List<OutputConfiguration> outputs)
    {
        this.outputs = outputs;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
