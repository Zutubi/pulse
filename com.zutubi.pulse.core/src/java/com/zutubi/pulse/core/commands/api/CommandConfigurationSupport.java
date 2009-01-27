package com.zutubi.pulse.core.commands.api;

import com.zutubi.pulse.core.Command;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wizard;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;

import java.util.LinkedList;
import java.util.List;

/**
 * Abstract base class to support implementation of the {@link com.zutubi.pulse.core.commands.api.CommandConfiguration}
 * interface.  Provides simple implementations of common properties.
 */
@SymbolicName("zutubi.commandConfigSupport")
public abstract class CommandConfigurationSupport extends AbstractNamedConfiguration implements CommandConfiguration
{
    private Class<? extends Command> commandType;
    @Wizard.Ignore
    private boolean force = false;
    private List<OutputConfiguration> outputs = new LinkedList<OutputConfiguration>();

    protected CommandConfigurationSupport(Class<? extends Command> commandType)
    {
        this.commandType = commandType;
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

    public Class<? extends Command> commandType()
    {
        return commandType;
    }
}
