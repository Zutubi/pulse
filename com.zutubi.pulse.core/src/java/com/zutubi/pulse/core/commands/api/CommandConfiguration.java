package com.zutubi.pulse.core.commands.api;

import com.zutubi.pulse.core.Command;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.NamedConfiguration;

import java.util.List;

/**
 * Base interface for all command configuration types.  Defines common
 * properties, and a factory method used to build
 * {@link com.zutubi.pulse.core.Command} instances from the configuration.
 */
@SymbolicName("zutubi.commandConfig")
public interface CommandConfiguration extends NamedConfiguration
{
    /**
     * Indicates if the command should be executed even if the recipe has
     * already failed.
     *
     * @return true if the command is forced to execute
     */
    boolean isForce();

    /**
     * Set to true to force the command to execute even though the recipe has
     * already failed, false to skip this command in that case.
     *
     * @param force the new value of the force flag
     */
    void setForce(boolean force);

    List<OutputConfiguration> getOutputs();

    void setOutputs(List<OutputConfiguration> outputs);

    Class<? extends Command> commandType();
}
