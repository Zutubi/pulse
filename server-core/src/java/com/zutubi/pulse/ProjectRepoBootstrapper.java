/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse;

import com.zutubi.pulse.core.Bootstrapper;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.PulseException;
import com.zutubi.pulse.core.RecipePaths;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.model.Scm;
import com.zutubi.pulse.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;

/**
 * The Project Repo Bootstrapper checks out a project into a the project-root/project-name/repo
 * directory and then runs an update when necessary, copying the results into the build directory.
 *
 */
public class ProjectRepoBootstrapper implements Bootstrapper
{
    /**
     * The local scm working directory.
     */
    private final File localDir;

    private final Scm scm;
    private ScmBootstrapper bootstrapper;
    private boolean checkoutOnly;

    public ProjectRepoBootstrapper(File localDir, Scm scm, boolean checkoutOnly)
    {
        this.localDir = localDir;
        this.scm = scm;
        this.checkoutOnly = checkoutOnly;
    }

    public void prepare() throws PulseException
    {
        if (!localDir.exists() && !localDir.mkdirs())
        {
            throw new PulseException("Failed to initialise local scm directory: " + localDir.getAbsolutePath());
        }

        bootstrapper = selectBootstrapper();
        bootstrapper.prepare();
    }

    public Revision getRevision()
    {
        if (bootstrapper != null)
        {
            return bootstrapper.getRevision();
        }
        return null;
    }

    public void bootstrap(RecipePaths paths) throws BuildException
    {
        if (checkoutOnly)
        {
            // bootstrap the recipe paths directly.
            bootstrapper.bootstrap(paths);
            return;
        }

        // run the scm bootstrapper on the local directory,
        bootstrapper.bootstrap(new RecipePaths()
        {
            public File getBaseDir()
            {
                return localDir;
            }

            public File getOutputDir()
            {
                return null;
            }
        });

        // copy these details to the base directory.
        File baseDir = paths.getBaseDir();
        if (baseDir.exists() && !baseDir.delete())
        {
            throw new BuildException("Unable to bootstrap " + baseDir);
        }
        try
        {
            FileSystemUtils.copyRecursively(localDir, baseDir);
        }
        catch (IOException e)
        {
            throw new BuildException(e);
        }
    }

    private ScmBootstrapper selectBootstrapper()
    {
        if (checkoutOnly)
        {
            return new CheckoutBootstrapper(scm);
        }
        // else we can update.
        if (localDir.list().length == 0)
        {
            return new CheckoutBootstrapper(scm);
        }
        else
        {
            return new UpdateBootstrapper(scm);
        }
    }
}
