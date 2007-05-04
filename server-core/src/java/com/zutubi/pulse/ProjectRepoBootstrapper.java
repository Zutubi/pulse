package com.zutubi.pulse;

import com.zutubi.pulse.core.*;
import com.zutubi.pulse.servercore.config.ScmConfiguration;
import com.zutubi.pulse.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;

/**
 * The Project Repo Bootstrapper checks out a project into the:
 *    work/project-name/spec-name
 * directory and then runs an update when necessary, copying the results into
 * the base directory when it differs from the checkout directory.
 */
public class ProjectRepoBootstrapper implements Bootstrapper
{
    private final String projectName;
    private final ScmConfiguration scm;
    private BuildRevision revision;
    private String agent;
    private boolean forceClean;
    private ScmBootstrapper childBootstrapper;

    public ProjectRepoBootstrapper(String projectName, ScmConfiguration scm, BuildRevision revision, boolean forceClean)
    {
        this.projectName = projectName;
        this.scm = scm;
        this.revision = revision;
        this.forceClean = forceClean;
    }

    public void bootstrap(final CommandContext context) throws BuildException
    {
        final RecipePaths paths = context.getPaths();
        if (paths.getPersistentWorkDir() == null)
        {
            throw new BuildException("Attempt to use update bootstrapping when no persistent working directory is available.");
        }

        // run the scm bootstrapper on the local directory,
        childBootstrapper = selectBootstrapper(paths.getPersistentWorkDir());
        childBootstrapper.prepare(agent);

        RecipePaths mungedPaths = new RecipePaths()
        {
            public File getPersistentWorkDir()
            {
                return paths.getPersistentWorkDir();
            }

            public File getBaseDir()
            {
                return paths.getPersistentWorkDir();
            }

            public File getOutputDir()
            {
                return paths.getOutputDir();
            }
        };

        context.setRecipePaths(mungedPaths);
        try
        {
            childBootstrapper.bootstrap(context);
        }
        finally
        {
            context.setRecipePaths(paths);
        }

        // If the checkout and base differ, then we need to copy over to the base.
        if(!paths.getBaseDir().equals(paths.getPersistentWorkDir()))
        {
            try
            {
                FileSystemUtils.copy(paths.getBaseDir(), paths.getPersistentWorkDir());
            }
            catch (IOException e)
            {
                throw new BuildException(e);
            }
        }
    }

    public void prepare(String agent)
    {
        this.agent = agent;
    }

    public void terminate()
    {
        if(childBootstrapper != null)
        {
            childBootstrapper.terminate();
        }
    }

    private ScmBootstrapper selectBootstrapper(final File localDir)
    {
        if(forceClean && localDir.exists())
        {
            if(!FileSystemUtils.rmdir(localDir))
            {
                throw new BuildException("Unable to remove local scm directory: " + localDir.getAbsolutePath());
            }
        }

        if (!localDir.exists() && !localDir.mkdirs())
        {
            throw new BuildException("Failed to initialise local scm directory: " + localDir.getAbsolutePath());
        }

        // else we can update.
        if (localDir.list().length == 0)
        {
            return new CheckoutBootstrapper(projectName, scm, revision, true);
        }
        else
        {
            return new UpdateBootstrapper(projectName, scm, revision);
        }
    }
}
