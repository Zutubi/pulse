package com.zutubi.pulse.servercore;

import com.zutubi.pulse.core.Bootstrapper;
import com.zutubi.pulse.core.BootstrapperSupport;
import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.engine.api.BuildException;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class ChainBootstrapper extends BootstrapperSupport
{
    private List<Bootstrapper> bootstrappers = new LinkedList<Bootstrapper>();
    private Bootstrapper currentBootstrapper;

    public ChainBootstrapper(Bootstrapper ...bootstrappers)
    {
        this.bootstrappers.addAll(Arrays.asList(bootstrappers));
    }

    public void bootstrap(CommandContext commandContext) throws BuildException
    {
        for (Bootstrapper bootstrapper : bootstrappers)
        {
            currentBootstrapper = bootstrapper;
            bootstrapper.bootstrap(commandContext);
        }

        currentBootstrapper = null;
    }

    public ChainBootstrapper add(Bootstrapper bootstrapper)
    {
        bootstrappers.add(bootstrapper);
        return this;
    }

    public void terminate()
    {
        if(currentBootstrapper != null)
        {
            currentBootstrapper.terminate();
        }
    }
}
