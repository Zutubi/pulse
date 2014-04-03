package com.zutubi.pulse.servercore;

import com.zutubi.pulse.core.BootstrapperSupport;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.RecipePaths;
import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.util.StringUtils;
import com.zutubi.util.io.FileSystemUtils;

import java.io.File;
import java.io.IOException;

import static com.zutubi.pulse.core.engine.api.BuildProperties.*;
import static com.zutubi.util.io.FileSystemUtils.*;

/**
 * The Project Repository Bootstrapper checks out a project into the project recipes
 * persistent working directory and then runs an update when necessary. The results
 * are then copied into the builds base directory if it differs from the checkout
 * directory.
 */
public class ProjectBootstrapper extends BootstrapperSupport
{
    private final String projectName;
    private final BuildRevision revision;
    private ScmBootstrapper childBootstrapper;

    public ProjectBootstrapper(String projectName, BuildRevision revision)
    {
        this.projectName = projectName;
        this.revision = revision;
    }

    public void doBootstrap(final CommandContext commandContext) throws BuildException
    {
        PulseExecutionContext context = (PulseExecutionContext) commandContext.getExecutionContext();
        final RecipePaths paths = context.getValue(NAMESPACE_INTERNAL, PROPERTY_RECIPE_PATHS, RecipePaths.class);

        File checkoutDir = paths.getCheckoutDir();
        if (checkoutDir != null)
        {
            String ignoreDirs;

            final String checkoutSubdir = context.getString(NAMESPACE_INTERNAL, PROPERTY_CHECKOUT_SUBDIR);
            checkoutDir = applySubdir(checkoutDir, checkoutSubdir);

            childBootstrapper = selectBootstrapper(checkoutDir);

            context.push();
            try
            {
                context.setWorkingDir(checkoutDir);
                childBootstrapper.bootstrap(commandContext);
                ignoreDirs = context.getString(NAMESPACE_INTERNAL, PROPERTY_IGNORE_DIRS);
            }
            finally
            {
                context.setWorkingDir(paths.getBaseDir());
                context.pop();
            }

            File sourceDir = applySubdir(paths.getBaseDir(), checkoutSubdir);
            ensureDirectory(sourceDir);
            if (!sourceDir.equals(checkoutDir))
            {
                writeFeedback("Copying source from " + getNormalisedAbsolutePath(checkoutDir) +
                        " to " + getNormalisedAbsolutePath(sourceDir) + ".  This may take some time.");
                try
                {
                    if (StringUtils.stringSet(ignoreDirs))
                    {
                        writeFeedback("Excluding directories with names: '" + ignoreDirs + "'.");
                        String[] excludes = StringUtils.split(ignoreDirs, ',');
                        ensureEmptyDirectory(sourceDir);
                        recursiveCopy(sourceDir, checkoutDir, excludes);
                    }
                    else
                    {
                        copy(sourceDir, checkoutDir);
                    }
                }
                catch (IOException e)
                {
                    throw new BuildException(e);
                }
            }
        }
    }

    private File applySubdir(File dir, String subdir)
    {
        if (StringUtils.stringSet(subdir))
        {
            return new File(dir, subdir);
        }
        else
        {
            return dir;
        }
    }

    public void terminate()
    {
        super.terminate();
        if (childBootstrapper != null)
        {
            childBootstrapper.terminate();
        }
    }

    private ScmBootstrapper selectBootstrapper(final File localDir)
    {
        ensureDirectory(localDir);
        if (FileSystemUtils.list(localDir).length == 0)
        {
            return new CheckoutBootstrapper(projectName, revision);
        }
        else // else we can update.
        {
            return new UpdateBootstrapper(projectName, revision);
        }
    }

    private void ensureDirectory(File localDir)
    {
        if (!localDir.exists() && !localDir.mkdirs())
        {
            throw new BuildException("Failed to initialise directory: " + localDir.getAbsolutePath());
        }
    }
}
