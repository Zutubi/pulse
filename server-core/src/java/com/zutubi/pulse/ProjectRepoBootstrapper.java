package com.zutubi.pulse;

import com.zutubi.pulse.core.Bootstrapper;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.CommandContext;
import com.zutubi.pulse.core.RecipePaths;
import com.zutubi.pulse.core.scm.config.ScmConfiguration;
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
    private final ScmConfiguration scmConfig;
    private BuildRevision revision;
    private String agent;
    private ScmBootstrapper childBootstrapper;

    public ProjectRepoBootstrapper(String projectName, ScmConfiguration scmConfig, BuildRevision revision)
    {
        this.projectName = projectName;
        this.scmConfig = scmConfig;
        this.revision = revision;
    }

    public void bootstrap(final CommandContext context) throws BuildException
    {
        final RecipePaths paths = context.getPaths();
        if (paths.getPersistentWorkDir() == null)
        {
            throw new BuildException("Attempt to use update bootstrapping when no persistent working directory is available.");
        }

        // run the scm bootstrapper on the local directory,
        BuildContext buildContext = context.getBuildContext();
        boolean cleanBuild = buildContext != null && buildContext.isCleanBuild();
        childBootstrapper = selectBootstrapper(cleanBuild, paths.getPersistentWorkDir());
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

    private ScmBootstrapper selectBootstrapper(boolean cleanBuild, final File localDir)
    {
        if(cleanBuild && localDir.exists())
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
            return new CheckoutBootstrapper(projectName, scmConfig, revision, true);
        }
        else
        {
            return new UpdateBootstrapper(projectName, scmConfig, revision);
        }
    }
}
