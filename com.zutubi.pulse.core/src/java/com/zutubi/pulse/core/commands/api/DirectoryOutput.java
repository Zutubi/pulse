package com.zutubi.pulse.core.commands.api;

import com.zutubi.pulse.core.engine.api.BuildException;
import org.apache.tools.ant.DirectoryScanner;

import java.io.File;
import java.util.List;

/**
 */
public class DirectoryOutput extends FileSystemOutputSupport<DirectoryOutputConfiguration>
{
    public DirectoryOutput(DirectoryOutputConfiguration config)
    {
        super(config);
    }

    protected void captureFiles(File toDir, CommandContext context)
    {
        DirectoryOutputConfiguration config = getConfig();
        File base = config.getBase();
        if (base == null)
        {
            base = context.getExecutionContext().getWorkingDir();
        }
        else if (!base.isAbsolute())
        {
            base = new File(context.getExecutionContext().getWorkingDir(), base.getPath());
        }

        if (!base.exists())
        {
            if(config.isFailIfNotPresent() && ! context.getResultState().isBroken())
            {
                throw new BuildException("Capturing artifact '" + config.getName() + "': base directory '" + base.getAbsolutePath() + "' does not exist");
            }
            else
            {
                // Don't attempt to capture.
                return;
            }
        }

        if (!base.isDirectory())
        {
            throw new BuildException("Directory artifact '" + config.getName() + "': base '" + base.getAbsolutePath() + "' is not a directory");
        }

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(base);
        if (!config.getInclusions().isEmpty())
        {
            scanner.setIncludes(flattenPatterns(config.getInclusions()));
        }

        if (!config.getExclusions().isEmpty())
        {
            scanner.setExcludes(flattenPatterns(config.getExclusions()));
        }

        scanner.setFollowSymlinks(config.isFollowSymlinks());
        scanner.scan();

        context.setOutputIndex(config.getName(), config.getIndex());
        for (String file : scanner.getIncludedFiles())
        {
            captureFile(new File(toDir, file), new File(base, file), context);
        }
    }

    private String[] flattenPatterns(List<DirectoryOutputConfiguration.PatternConfiguration> patterns)
    {
        String[] result = new String[patterns.size()];
        int i = 0;

        for (DirectoryOutputConfiguration.PatternConfiguration p : patterns)
        {
            result[i++] = p.getPattern();
        }

        return result;
    }
}