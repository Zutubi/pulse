package com.zutubi.pulse.core.commands.api;

import com.zutubi.pulse.core.engine.api.BuildException;
import org.apache.tools.ant.DirectoryScanner;

import java.io.File;

/**
 * An artifact that captures a single file.
 *
 * @see FileArtifactConfiguration
 */
public class FileArtifact extends FileSystemArtifactSupport
{
    /**
     * Constructor which stores the configuration.
     *
     * @param config configuration for this artifact
     * @see #getConfig() 
     */
    public FileArtifact(FileArtifactConfiguration config)
    {
        super(config);
    }

    protected void captureFiles(File toDir, CommandContext context)
    {
        FileArtifactConfiguration config = (FileArtifactConfiguration) getConfig();
        String file = config.getFile();

        File captureFile = new File(file);
        boolean absolutePathSpecified = isAbsolute(captureFile);
        if (!absolutePathSpecified)
        {
            captureFile = new File(context.getExecutionContext().getWorkingDir(), file);
        }

        if (captureFile.isFile())
        {
            // Simple case: just capture it.
            if (!captureFile(new File(toDir, captureFile.getName()), captureFile, context)  && config.isFailIfNotPresent())
            {
                throw new BuildException("Capturing artifact '" + config.getName() + "': existing file '" + file + "' is stale");
            }
            return;
        }

        // If the specified path was absolute with wildcards, then we need to
        // jump through a few hoops.
        if (absolutePathSpecified)
        {
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
        boolean singleFile = scanner.getIncludedFilesCount() == 1;
        for (String includedFile : scanner.getIncludedFiles())
        {
            File dest = new File(toDir, includedFile);
            if (singleFile)
            {
                // Captured paths use just the file name unless we capture multiple
                // files, in which case their paths are used.  Note that capturing
                // multiple files should really be done with a DirectoryArtifact.
                dest = new File(toDir, dest.getName());
            }

            File source = new File(baseDir, includedFile);
            if (captureFile(dest, source, context))
            {
                fileCaptured = true;
            }
        }

        FileArtifactConfiguration config = (FileArtifactConfiguration) getConfig();
        if (!fileCaptured && config.isFailIfNotPresent() && !context.getResultState().isBroken())
        {
            if (scanner.getIncludedFilesCount() == 0)
            {
                throw new BuildException("Capturing artifact '" + getConfig().getName() + "': no file matching '" + file + "' exists");
            }
            else
            {
                throw new BuildException("Capturing artifact '" + getConfig().getName() + "': all files matching '" + file + "' are stale");
            }
        }
    }
}
