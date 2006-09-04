package com.zutubi.pulse;

import com.zutubi.pulse.core.Bootstrapper;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.CommandContext;
import com.zutubi.pulse.core.PulseException;
import com.zutubi.pulse.personal.PatchArchive;

/**
 * A bootstrapper that applies a patch to a working directory bootstrapped
 * by some other bootstrapper.
 */
public class PatchBootstrapper implements Bootstrapper
{
    private Bootstrapper delegate;
    private PatchArchive patch;

    public PatchBootstrapper(Bootstrapper delegate, PatchArchive patch)
    {
        this.delegate = delegate;
        this.patch = patch;
    }

    public void bootstrap(CommandContext context) throws BuildException
    {
        delegate.bootstrap(context);
        try
        {
            patch.apply(context.getPaths().getBaseDir());
        }
        catch(PulseException e)
        {
            throw new BuildException("Unable to apply patch: " + e.getMessage(), e);
        }
    }

    public void prepare(String agent)
    {
        delegate.prepare(agent);
    }
}
