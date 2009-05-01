package com.zutubi.pulse.core;

import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.util.FileSystemUtils;
import org.apache.tools.ant.DirectoryScanner;

import java.io.File;

/**
 * Information about a single file artifact to be captured.
 */
public class FileArtifact extends LocalArtifact
{
    private String file;
    private String type = null;

    public FileArtifact()
    {

    }

    public void capture(CommandResult result, ExecutionContext context)
    {
        // The specified file may or may not be absolute.  If it is absolute, then we need to jump through a few
        // hoops.
        File captureFile = new File(file);
        if (captureFile.isAbsolute())
        {
            if (captureFile.isFile())
            {
                // excellent, we have the file, we can capture it and continue.
                StoredArtifact artifact = new StoredArtifact(getName());
                if(captureFile(artifact, captureFile, FileSystemUtils.composeFilename(getName(), captureFile.getName()), result, context, type))
                {
                    result.addArtifact(artifact);
                }
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

                scanAndCaptureFiles(alternateBaseDir, filePath, result, context);
            }
            else
            {
                if (result.succeeded() && getFailIfNotPresent())
                {
                    result.error("Capturing artifact '" + getName() + "': no file matching '" + file + "' exists");
                }
            }
        }
        else
        {
            // The file path is relative, we have our base directory, lets get to work.
            scanAndCaptureFiles(context.getWorkingDir(), file, result, context);
        }
    }

    private void scanAndCaptureFiles(File baseDir, String file, CommandResult result, ExecutionContext context)
    {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(baseDir);
        scanner.setIncludes(new String[]{file});
        scanner.scan();

        StoredArtifact artifact = new StoredArtifact(getName());
        for (String includedFile : scanner.getIncludedFiles())
        {
            File source = new File(baseDir, includedFile);
            captureFile(artifact, source, FileSystemUtils.composeFilename(getName(), includedFile), result, context, type);
        }
        if (artifact.getChildren().size() > 0)
        {
            result.addArtifact(artifact);
        }
        else
        {
            if (result.succeeded() && getFailIfNotPresent())
            {
                result.error("Capturing artifact '" + getName() + "': no file matching '" + file + "' exists");
            }
        }
    }

    public String getFile()
    {
        return file;
    }

    public void setFile(String file)
    {
        this.file = file;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

}
