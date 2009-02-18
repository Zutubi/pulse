package com.zutubi.pulse.core.commands.api;

import com.zutubi.pulse.core.engine.api.BuildException;
import static com.zutubi.pulse.core.engine.api.BuildProperties.NAMESPACE_INTERNAL;
import static com.zutubi.pulse.core.engine.api.BuildProperties.PROPERTY_RECIPE_TIMESTAMP_MILLIS;
import com.zutubi.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;

/**
 */
public abstract class FileSystemOutputSupport<T extends FileSystemOutputConfigurationSupport> extends OutputSupport<T>
{
    protected FileSystemOutputSupport(T config)
    {
        super(config);
    }

    public void capture(CommandContext context)
    {
        FileSystemOutputConfigurationSupport config = getConfig();
        File file = context.registerOutput(config.getName(), config.getType());
        captureFiles(file, context);
        context.registerProcessors(config.getName(), config.getPostProcessors());
    }

    protected boolean captureFile(File toFile, File fromFile, CommandContext context)
    {
        long recipeTimestamp = context.getExecutionContext().getLong(NAMESPACE_INTERNAL, PROPERTY_RECIPE_TIMESTAMP_MILLIS, 0);
        if (getConfig().isIgnoreStale() && fromFile.lastModified() < recipeTimestamp)
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

    protected abstract void captureFiles(File toDir, CommandContext context);
}
