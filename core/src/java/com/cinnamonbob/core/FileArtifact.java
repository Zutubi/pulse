package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredArtifact;

import java.io.*;

/**
 * Information about a single file artifact to be captured.
 */
public class FileArtifact extends Artifact
{
    private File file;
    private String type = null;

    public FileArtifact()
    {

    }

    public void capture(CommandResult result, File baseDir, File outputDir)
    {
        StoredArtifact stored = new StoredArtifact(getName());
        File captureFile;
        if(file.isAbsolute())
        {
            captureFile = file;
        }
        else
        {
            captureFile = new File(baseDir, file.getPath());
        }

        if(!captureFile.exists())
        {
            if(getFailIfNotPresent())
            {
                throw new BuildException("Capturing artifact '" + getName() + "': file '" + captureFile.getAbsolutePath() + "' does not exist");
            }
            else
            {
                // Don't attempt to capture.
                return;
            }
        }

        captureFile(stored, captureFile, file.getName(), outputDir, result, type);
        result.addArtifact(stored);
    }

    public FileArtifact(String name, File file)
    {
        super(name);
        this.file = file;
    }

    public File getFile()
    {
        return file;
    }

    public void setFile(File file)
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
