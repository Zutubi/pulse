package com.zutubi.pulse;

import com.zutubi.pulse.core.*;
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
    private BuildRevision revision;

    public ProjectRepoBootstrapper(File localDir, Scm scm, BuildRevision revision)
    {
        this.localDir = localDir;
        this.scm = scm;
        this.revision = revision;
    }

    public void bootstrap(RecipePaths paths) throws BuildException
    {
        // run the scm bootstrapper on the local directory,
        ScmBootstrapper bootstrapper = selectBootstrapper();
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
        if (!localDir.exists() && !localDir.mkdirs())
        {
            throw new BuildException("Failed to initialise local scm directory: " + localDir.getAbsolutePath());
        }

        // else we can update.
        if (localDir.list().length == 0)
        {
            return new CheckoutBootstrapper(scm, revision);
        }
        else
        {
            return new UpdateBootstrapper(scm, revision);
        }
    }
}
