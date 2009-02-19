package com.zutubi.pulse.core.commands.api;

import com.zutubi.pulse.core.Command;
import com.zutubi.pulse.core.plugins.PostProcessorExtensionManager;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Transient;
import com.zutubi.tove.annotations.Wire;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Abstract base class to support implementation of the {@link com.zutubi.pulse.core.commands.api.CommandConfiguration}
 * interface.  Provides simple implementations of common properties.
 */
@SymbolicName("zutubi.commandConfigSupport")
@Wire
public abstract class CommandConfigurationSupport extends AbstractNamedConfiguration implements CommandConfiguration
{
    private Class<? extends Command> commandType;
    private boolean force = false;
    private Map<String, OutputConfiguration> outputs = new LinkedHashMap<String, OutputConfiguration>();

    @Transient
    private PostProcessorExtensionManager postProcessorExtensionManager;

    protected CommandConfigurationSupport(Class<? extends Command> commandType)
    {
        this.commandType = commandType;
    }

    public void initialiseSingleCommandProject(Map<String, PostProcessorConfiguration> availableProcessors)
    {
        // Nothing to do by default.
    }

    protected String getDefaultPostProcessorName(Class<? extends PostProcessorConfiguration> type)
    {
        return postProcessorExtensionManager.getDefaultProcessorName(type);
    }

    public boolean isForce()
    {
        return force;
    }

    public void setForce(boolean force)
    {
        this.force = force;
    }

    public Map<String, OutputConfiguration> getOutputs()
    {
        return outputs;
    }

    public void setOutputs(Map<String, OutputConfiguration> outputs)
    {
        this.outputs = outputs;
    }

    public Class<? extends Command> commandType()
    {
        return commandType;
    }

    public void setPostProcessorExtensionManager(PostProcessorExtensionManager postProcessorExtensionManager)
    {
        this.postProcessorExtensionManager = postProcessorExtensionManager;
    }
}
