package com.zutubi.pulse.prototype.config.types;

import com.zutubi.validation.annotations.Required;

/**
 *
 *
 */
public class FileArtifactConfiguration extends ArtifactConfiguration
{
    /**
     * Path of the file to capture, relative to the base directory.
     */
    @Required()
    private String file;

    /**
     * MIME type of the file, may be null in which case it will be guessed
     * when the user downloads the artifact.
     */
    private String mimeType;

    public String getFile()
    {
        return file;
    }

    public void setFile(String file)
    {
        this.file = file;
    }

    public String getMimeType()
    {
        return mimeType;
    }

    public void setMimeType(String mimeType)
    {
        this.mimeType = mimeType;
    }
}
