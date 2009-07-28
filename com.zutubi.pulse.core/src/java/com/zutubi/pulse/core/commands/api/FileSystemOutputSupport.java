package com.zutubi.pulse.core.commands.api;

import com.zutubi.pulse.core.engine.api.BuildException;
import static com.zutubi.pulse.core.engine.api.BuildProperties.NAMESPACE_INTERNAL;
import static com.zutubi.pulse.core.engine.api.BuildProperties.PROPERTY_RECIPE_TIMESTAMP_MILLIS;
import com.zutubi.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;

/**
 * Support base class for output that captures files from the base directory
 * into the output directory.
 */
public abstract class FileSystemOutputSupport extends OutputSupport
{
    /**
     * Constructor that stores the configuration.
     *
     * @param config configuration for this output
     * @see #getConfig()
     */
    protected FileSystemOutputSupport(FileSystemOutputConfigurationSupport config)
    {
        super(config);
    }

    public void capture(CommandContext context)
    {
        FileSystemOutputConfigurationSupport config = (FileSystemOutputConfigurationSupport) getConfig();
        File file = context.registerOutput(config.getName(), config.getType());
        captureFiles(file, context);
        if (config.isPublish())
        {
            context.markOutputForPublish(config.getName(), config.getArtifactPattern());
        }
        context.registerProcessors(config.getName(), config.getPostProcessors());
    }

    /**
     * Helper method to capture a single file in a consistent way.
     *
     * @param toFile   destination file (should be within the output directory)
     * @param fromFile source file (within the base directory)
     * @param context  context in which the command is executing
     * @return true if the file was capture, false if it was skipped
     * @throws BuildException is there is an I/O error
     */
    protected boolean captureFile(File toFile, File fromFile, CommandContext context)
    {
        FileSystemOutputConfigurationSupport config = (FileSystemOutputConfigurationSupport) getConfig();
        long recipeTimestamp = context.getExecutionContext().getLong(NAMESPACE_INTERNAL, PROPERTY_RECIPE_TIMESTAMP_MILLIS, 0);
        if (config.isIgnoreStale() && fromFile.lastModified() < recipeTimestamp)
        {
            return false;
        }

        File parent = toFile.getParentFile();
        try
        {
            FileSystemUtils.createDirectory(parent);
            FileSystemUtils.copy(toFile, fromFile);
            return true;
        }
        catch (IOException e)
        {
            throw new BuildException("Unable to collect file '" + fromFile.getAbsolutePath() + "' for output '" + getConfig().getName() + "': " + e.getMessage(), e);
        }
    }

    /**
     * Method to implement to find and capture the desired files.  The method
     * should identify individual files to capture and use {@link #captureFile(java.io.File, java.io.File, CommandContext)}
     * to do the actual capturing.
     *
     * @param toDir   the output directory to which files should be captured
     * @param context ontext in which the command is executing
     */
    protected abstract void captureFiles(File toDir, CommandContext context);
}
