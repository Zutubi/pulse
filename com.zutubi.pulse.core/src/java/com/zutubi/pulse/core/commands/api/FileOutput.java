package com.zutubi.pulse.core.commands.api;

import com.zutubi.pulse.core.engine.api.BuildException;
import org.apache.tools.ant.DirectoryScanner;

import java.io.File;

/**
 * An output capture that captures a single file.
 *
 * @see com.zutubi.pulse.core.commands.api.FileOutputConfiguration
 */
public class FileOutput extends FileSystemOutputSupport
{
    /**
     * Constructor which stores the configuration.
     *
     * @param config configuration for this output
     * @see #getConfig() 
     */
    public FileOutput(FileOutputConfiguration config)
    {
        super(config);
    }

    protected void captureFiles(File toDir, CommandContext context)
    {
        FileOutputConfiguration config = (FileOutputConfiguration) getConfig();
        String file = config.getFile();
        File captureFile = new File(file);

        // The specified file may or may not be absolute.  If it is absolute, then we need to jump through a few
        // hoops.
        if (captureFile.isAbsolute())
        {
            if (captureFile.isFile())
            {
                // excellent, we have the file, we can capture it and continue.
                captureFile(new File(toDir, captureFile.getName()), captureFile, context);
                return;
            }

            // does the file contain any wild cards?
            if (file.indexOf("*") != -1)
            {
                // if so, then take the directory immediately above the wild card and use it as the base directory
                // for the scan.

                String filePath = captureFile.getAbsolutePath();
                File alternateBaseDir = new File(filePath.substring(0, filePath.indexOf('*')));
                if (!alternateBaseDir.isDirectory())
                {
                    // this will be the case if the wildcard is embedded within a filename, such as file*.txt
                    alternateBaseDir = alternateBaseDir.getParentFile();
                }
                filePath = filePath.substring(alternateBaseDir.getAbsolutePath().length());
                if (filePath.startsWith("/") || filePath.startsWith("\\"))
                {
                    filePath = filePath.substring(1);
                }

                scanAndCaptureFiles(toDir, alternateBaseDir, filePath, context);
            }
            else
            {
                // absolute file without wildcards that does not exist. This will be picked up by the getFailIfNotPresent
                // check at the end of this method.
                if (config.isFailIfNotPresent() && !context.getResultState().isBroken())
                {
                    throw new BuildException("Capturing artifact '" + config.getName() + "': no file matching '" + file + "' exists");
                }
            }
        }
        else
        {
            // The file path is relative, we have our base directory, lets get to work.
            scanAndCaptureFiles(toDir, context.getExecutionContext().getWorkingDir(), file, context);
        }
    }

    private void scanAndCaptureFiles(File toDir, File baseDir, String file, CommandContext context)
    {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(baseDir);
        scanner.setIncludes(new String[]{file});
        scanner.scan();

        boolean fileCaptured = false;
        for (String includedFile : scanner.getIncludedFiles())
        {
            File dest = new File(toDir, includedFile);
            File source = new File(baseDir, includedFile);
            if (captureFile(dest, source, context))
            {
                fileCaptured = true;
            }
        }

        FileOutputConfiguration config = (FileOutputConfiguration) getConfig();
        if (!fileCaptured && config.isFailIfNotPresent() && !context.getResultState().isBroken())
        {
            throw new BuildException("Capturing artifact '" + getConfig().getName() + "': no file matching '" + file + "' exists");
        }
    }
}
