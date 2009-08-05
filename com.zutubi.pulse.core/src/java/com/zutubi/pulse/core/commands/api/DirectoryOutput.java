package com.zutubi.pulse.core.commands.api;

import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.util.TextUtils;
import org.apache.tools.ant.DirectoryScanner;

import java.io.File;

/**
 * An output capture that captures a set of files under a base directory.
 *
 * @see com.zutubi.pulse.core.commands.api.DirectoryOutputConfiguration
 */
public class DirectoryOutput extends FileSystemOutputSupport
{
    /**
     * Constructor that stores the configuration.
     *
     * @param config configuration for this output
     * @see #getConfig() 
     */
    public DirectoryOutput(DirectoryOutputConfiguration config)
    {
        super(config);
    }

    protected void captureFiles(File toDir, CommandContext context)
    {
        DirectoryOutputConfiguration config = (DirectoryOutputConfiguration) getConfig();
        String base = config.getBase();
        File baseDir;
        if (!TextUtils.stringSet(base))
        {
            baseDir = context.getExecutionContext().getWorkingDir();
        }
        else
        {
            baseDir = new File(base);
            if (!isAbsolute(baseDir))
            {
                baseDir = new File(context.getExecutionContext().getWorkingDir(), baseDir.getPath());
            }
        }

        if (!baseDir.exists())
        {
            if(config.isFailIfNotPresent() && ! context.getResultState().isBroken())
            {
                throw new BuildException("Capturing artifact '" + config.getName() + "': base directory '" + baseDir.getAbsolutePath() + "' does not exist");
            }
            else
            {
                // Don't attempt to capture.
                return;
            }
        }

        if (!baseDir.isDirectory())
        {
            throw new BuildException("Directory artifact '" + config.getName() + "': base '" + baseDir.getAbsolutePath() + "' is not a directory");
        }

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(baseDir);
        if (!config.getInclusions().isEmpty())
        {
            scanner.setIncludes(config.getInclusions().toArray(new String[config.getInclusions().size()]));
        }

        if (!config.getExclusions().isEmpty())
        {
            scanner.setExcludes(config.getExclusions().toArray(new String[config.getExclusions().size()]));
        }

        scanner.setFollowSymlinks(config.isFollowSymlinks());
        scanner.scan();

        context.setOutputIndex(config.getName(), config.getIndex());
        for (String file : scanner.getIncludedFiles())
        {
            captureFile(new File(toDir, file), new File(baseDir, file), context);
        }
    }
}