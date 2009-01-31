package com.zutubi.pulse.servercore;

import com.zutubi.pulse.core.Bootstrapper;
import com.zutubi.pulse.core.BootstrapperSupport;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.model.CommandResult;

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

    public void bootstrap(PulseExecutionContext context, CommandResult result) throws BuildException
    {
        for (Bootstrapper bootstrapper : bootstrappers)
        {
            currentBootstrapper = bootstrapper;
            bootstrapper.bootstrap(context, result);
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
