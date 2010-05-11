package com.zutubi.pulse.servercore;

import com.zutubi.pulse.core.Bootstrapper;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.RecipePaths;
import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.WebUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import static com.zutubi.pulse.core.engine.api.BuildProperties.*;
import static com.zutubi.util.FileSystemUtils.copy;
import static com.zutubi.util.FileSystemUtils.getNormalisedAbsolutePath;

/**
 * The Project Repo Bootstrapper checks out a project into the:
 *    work/project-name/spec-name
 * directory and then runs an update when necessary, copying the results into
 * the base directory when it differs from the checkout directory.
 */
public class ProjectRepoBootstrapper implements Bootstrapper
{
    private final String projectName;
    private BuildRevision revision;
    private ScmBootstrapper childBootstrapper;

    public ProjectRepoBootstrapper(String projectName, BuildRevision revision)
    {
        this.projectName = projectName;
        this.revision = revision;
    }

    public void bootstrap(final CommandContext commandContext) throws BuildException
    {
        PulseExecutionContext context = (PulseExecutionContext) commandContext.getExecutionContext();
        final RecipePaths paths = context.getValue(NAMESPACE_INTERNAL, PROPERTY_RECIPE_PATHS, RecipePaths.class);
        final File persistentWorkDir = paths.getPersistentWorkDir();
        if (persistentWorkDir == null)
        {
            throw new BuildException("Attempt to use update bootstrapping when no persistent working directory is available.");
        }

        checkForOldWorkDir(context, persistentWorkDir);

        // run the scm bootstrapper on the local directory,
        childBootstrapper = selectBootstrapper(persistentWorkDir);

        RecipePaths mungedPaths = new RecipePaths()
        {
            public File getPersistentWorkDir()
            {
                return persistentWorkDir;
            }

            public File getBaseDir()
            {
                return persistentWorkDir;
            }

            public File getOutputDir()
            {
                return paths.getOutputDir();
            }
        };

        context.push();
        try
        {
            context.addValue(NAMESPACE_INTERNAL, PROPERTY_RECIPE_PATHS, mungedPaths);
            context.setWorkingDir(mungedPaths.getBaseDir());
            childBootstrapper.bootstrap(commandContext);
        }
        finally
        {
            context.setWorkingDir(paths.getBaseDir());
            context.pop();
        }

        // If the checkout and base differ, then we need to copy over to the base, this implies a CLEAN_UPDATE
        // checkout scheme.
        if(!paths.getBaseDir().equals(persistentWorkDir))
        {
            try
            {
                // log this action to the build log.
                writeStatusMessage(context, "Copying source from " + getNormalisedAbsolutePath(persistentWorkDir) +
                        " to " + getNormalisedAbsolutePath(paths.getBaseDir()) + ".  This may take some time.");

                copy(paths.getBaseDir(), persistentWorkDir);
            }
            catch (IOException e)
            {
                throw new BuildException(e);
            }
        }
    }

    private void writeStatusMessage(ExecutionContext context, String message)
    {
        PrintWriter out = new PrintWriter(new OutputStreamWriter(context.getOutputStream()));
        out.println(message);
        out.flush();
    }

    /**
     * Checks for the existance of a pre-2.1 work directory under
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
                    writeStatusMessage(context, "Moving old working directory '" + getNormalisedAbsolutePath(candidateOldDir) + "' to new location '" + getNormalisedAbsolutePath(persistentWorkDir) + "'...");
                    if (candidateOldDir.renameTo(persistentWorkDir))
                    {
                        writeStatusMessage(context, "Move succeeded.");
                    }
                    else
                    {
                        writeStatusMessage(context, "Move failed.  A new working directory will be used.");
                    }
                }
            }
        }
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
        if (!localDir.exists() && !localDir.mkdirs())
        {
            throw new BuildException("Failed to initialise local scm directory: " + localDir.getAbsolutePath());
        }

        if (localDir.list().length == 0)
        {
            return new CheckoutBootstrapper(projectName, revision);
        }
        else // else we can update.
        {
            return new UpdateBootstrapper(projectName, revision);
        }
    }
}
