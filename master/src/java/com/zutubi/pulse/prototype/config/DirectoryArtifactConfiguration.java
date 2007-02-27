package com.zutubi.pulse.prototype.config;

/**
 *
 *
 */
public class DirectoryArtifactConfiguration extends BaseArtifactConfiguration
{
    /**
     * Base directory, or null to default to base for the build.
     */
    private String base;

    /**
     * Space-separated list of include patterns for filtering files to capture.
     */
    private String includes;

    /**
     * Space-separated list of exclude patterns for filtering files to capture.
     */
    private String excludes;

    /**
     * MIME type of the files, may be null in which case it will be guessed
     * when the user downloads the file.
     */
    private String mimeType;

    public String getBase()
    {
        return base;
    }

    public void setBase(String base)
    {
        this.base = base;
    }

    public String getIncludes()
    {
        return includes;
    }

    public void setIncludes(String includes)
    {
        this.includes = includes;
    }

    public String getExcludes()
    {
        return excludes;
    }

    public void setExcludes(String excludes)
    {
        this.excludes = excludes;
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
