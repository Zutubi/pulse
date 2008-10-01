package com.zutubi.pulse.servercore;

import com.zutubi.pulse.core.Bootstrapper;
import com.zutubi.pulse.core.BuildException;
import static com.zutubi.pulse.core.BuildProperties.*;
import com.zutubi.pulse.core.ExecutionContext;
import com.zutubi.pulse.core.PulseException;
import com.zutubi.pulse.core.personal.PatchArchive;
import com.zutubi.pulse.core.scm.FileStatus;
import com.zutubi.pulse.servercore.repository.FileRepository;

import java.io.File;

/**
 * A bootstrapper that applies a patch to a working directory bootstrapped
 * by some other bootstrapper.
 */
public class PatchBootstrapper implements Bootstrapper
{
    private static final String DEFAULT_PATCH_BOOSTRAP_PREFIX = "personal.patch.prefix.default";

    private static final String PATCH_BOOSTRAP_PREFIX = "personal.patch.prefix";

    private Bootstrapper delegate;
    private long userId;
    private long number;
    private FileStatus.EOLStyle localEOL;

    public PatchBootstrapper(Bootstrapper delegate, long userId, long number, FileStatus.EOLStyle localEOL)
    {
        this.delegate = delegate;
        this.userId = userId;
        this.number = number;
        this.localEOL = localEOL;
    }

    public void bootstrap(ExecutionContext context) throws BuildException
    {
        delegate.bootstrap(context);
        try
        {
            FileRepository fileRepository = context.getValue(NAMESPACE_INTERNAL, PROPERTY_FILE_REPOSITORY, FileRepository.class);
            PatchArchive patch = new PatchArchive(fileRepository.getPatchFile(userId, number));
            // apply a patch prefix to the if one is specified. Used to work around a cvs issue.
            patch.apply(getBaseBuildDir(context), localEOL);
        }
        catch(PulseException e)
        {
            throw new BuildException("Unable to apply patch: " + e.getMessage(), e);
        }
    }

    public void terminate()
    {
        delegate.terminate();
    }

    private File getBaseBuildDir(ExecutionContext context)
    {
        // check if we need to apply a patch prefix for this bootstrap. 

        String defaultPrefix = System.getProperty(DEFAULT_PATCH_BOOSTRAP_PREFIX);

        String projectPrefix = null;
        String projectName = context.getString(NAMESPACE_INTERNAL, PROPERTY_PROJECT);
        if (projectName != null)
        {
            projectPrefix = System.getProperty(PATCH_BOOSTRAP_PREFIX + "." + projectName);
        }

        String prefix = null;
        if (projectPrefix != null && !projectPrefix.equals(""))
        {
            prefix = projectPrefix;
        }
        else if (defaultPrefix != null && !defaultPrefix.equals(""))
        {
            prefix = defaultPrefix;
        }

        if (prefix != null)
        {
            return new File(context.getWorkingDir(), prefix);
        }
        return context.getWorkingDir();
    }
}
