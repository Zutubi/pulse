package com.zutubi.pulse.servercore;

import com.zutubi.pulse.core.BootstrapperSupport;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.RecipePaths;
import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.engine.api.BuildException;
import static com.zutubi.pulse.core.engine.api.BuildProperties.*;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.util.FileSystemUtils;
import static com.zutubi.util.FileSystemUtils.copy;
import static com.zutubi.util.FileSystemUtils.getNormalisedAbsolutePath;
import com.zutubi.util.WebUtils;

import java.io.File;
import java.io.IOException;

/**
 * The Project Repository Bootstrapper checks out a project into the project recipes
 * persistent working directory and then runs an update when necessary. The results
 * are then copied into the builds base directory if it differs from the checkout
 * directory.
 *
 * @see com.zutubi.pulse.core.scm.config.api.CheckoutScheme#CLEAN_UPDATE
 * @see com.zutubi.pulse.core.scm.config.api.CheckoutScheme#INCREMENTAL_UPDATE
 */
public class ProjectRepositoryBootstrapper extends BootstrapperSupport
{
    private final String projectName;
    private final BuildRevision revision;
    private ScmBootstrapper childBootstrapper;

    public ProjectRepositoryBootstrapper(String projectName, BuildRevision revision)
    {
        this.projectName = projectName;
        this.revision = revision;
    }

    public void doBootstrap(final CommandContext commandContext) throws BuildException
    {
        PulseExecutionContext context = (PulseExecutionContext) commandContext.getExecutionContext();
        final RecipePaths paths = context.getValue(NAMESPACE_INTERNAL, PROPERTY_RECIPE_PATHS, RecipePaths.class);
        final File persistentWorkDir = paths.getPersistentWorkDir();
        if (persistentWorkDir == null)
        {
            throw new BuildException("Attempt to use update bootstrapping when no persistent working directory is available.");
        }

        checkForOldWorkDir(context, persistentWorkDir);

        // scm bootstrappers work with the base directory, so re-map the paths such that
        // the base directory points to the persistent directory.
        RecipePaths bootstrapperPaths = new ProjectRepositoryBootstrapperRecipePaths(paths);

        childBootstrapper = selectBootstrapper(bootstrapperPaths.getBaseDir());

        context.push();
        try
        {
            context.addValue(NAMESPACE_INTERNAL, PROPERTY_RECIPE_PATHS, bootstrapperPaths);
            context.setWorkingDir(bootstrapperPaths.getBaseDir());
            childBootstrapper.bootstrap(commandContext);
        }
        finally
        {
            context.setWorkingDir(paths.getBaseDir());
            context.pop();
        }

        // If the builds base directory differs from the bootstrap base directly, then we need to copy over
        // to the base, this implies a CLEAN_UPDATE checkout scheme.
        if(!paths.getBaseDir().equals(bootstrapperPaths.getBaseDir()))
        {
            try
            {
                // log this action to the build log.
                writeFeedback("Copying source from " + getNormalisedAbsolutePath(bootstrapperPaths.getBaseDir()) +
                        " to " + getNormalisedAbsolutePath(paths.getBaseDir()) + ".  This may take some time.");

                copy(paths.getBaseDir(), bootstrapperPaths.getBaseDir());
            }
            catch (IOException e)
            {
                throw new BuildException(e);
            }
        }
    }

    /**
     * Checks for the existence of a pre-2.1 work directory under
     * ${data.dir}/work.  If it is found, and we have no current work
     * directory, then we assume we have upgraded and move it across.
     *
     * @param context           the execution context in which this check takes place
     * @param persistentWorkDir the new persistent working directory location
     */
    private void checkForOldWorkDir(ExecutionContext context, File persistentWorkDir)
    {
        if (!persistentWorkDir.exists())
        {
            File parentDir = persistentWorkDir.getParentFile();
            if (parentDir != null && (parentDir.isDirectory() || parentDir.mkdirs()))
            {
                File dataDir = context.getFile(NAMESPACE_INTERNAL, PROPERTY_DATA_DIR);
                File candidateOldDir = new File(dataDir, FileSystemUtils.composeFilename("work", WebUtils.formUrlEncode(projectName)));
                if (candidateOldDir.isDirectory())
                {
                    writeFeedback("Moving old working directory '" + getNormalisedAbsolutePath(candidateOldDir) + "' to new location '" + getNormalisedAbsolutePath(persistentWorkDir) + "'...");
                    if (candidateOldDir.renameTo(persistentWorkDir))
                    {
                        writeFeedback("Move succeeded.");
                    }
                    else
                    {
                        writeFeedback("Move failed.  A new working directory will be used.");
                    }
                }
            }
        }
    }

    public void terminate()
    {
        super.terminate();
        if(childBootstrapper != null)
        {
            childBootstrapper.terminate();
        }
    }

    private ScmBootstrapper selectBootstrapper(final File localDir)
    {
        if (!localDir.exists() && !localDir.mkdirs())
        {
            throw new BuildException("Failed to initialise local scm directory: " + localDir.getAbsolutePath());
        }

        if (FileSystemUtils.list(localDir).size() == 0)
        {
            return new CheckoutBootstrapper(projectName, revision);
        }
        else // else we can update.
        {
            return new UpdateBootstrapper(projectName, revision);
        }
    }

    /**
     * The recipe paths used by this bootstrapper re-map the base directory to the
     * projects persistent working directory.
     */
    private static class ProjectRepositoryBootstrapperRecipePaths implements RecipePaths
    {
        private RecipePaths paths;

        private ProjectRepositoryBootstrapperRecipePaths(RecipePaths paths)
        {
            this.paths = paths;
        }

        public File getOutputDir()
        {
            return paths.getOutputDir();
        }

        public File getBaseDir()
        {
            return paths.getPersistentWorkDir();
        }

        public File getPersistentWorkDir()
        {
            return paths.getPersistentWorkDir();
        }
    }
}
