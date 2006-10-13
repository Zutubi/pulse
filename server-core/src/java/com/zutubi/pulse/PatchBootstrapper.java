package com.zutubi.pulse;

import com.zutubi.pulse.core.Bootstrapper;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.CommandContext;
import com.zutubi.pulse.core.PulseException;
import com.zutubi.pulse.personal.PatchArchive;
import com.zutubi.pulse.repository.FileRepository;

/**
 * A bootstrapper that applies a patch to a working directory bootstrapped
 * by some other bootstrapper.
 */
public class PatchBootstrapper implements Bootstrapper
{
    private Bootstrapper delegate;
    private long userId;
    private long number;

    public PatchBootstrapper(Bootstrapper delegate, long userId, long number)
    {
        this.delegate = delegate;
        this.userId = userId;
        this.number = number;
    }

    public void bootstrap(CommandContext context) throws BuildException
    {
        delegate.bootstrap(context);
        try
        {
            FileRepository fileRepository = context.getBuildContext().getFileRepository();
            PatchArchive patch = new PatchArchive(fileRepository.getPatchFile(userId, number));
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
