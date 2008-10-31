package com.zutubi.pulse.master.tove.config.project.types;

import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Transient;
import com.zutubi.util.StringUtils;
import com.zutubi.util.TextUtils;

import java.util.Collections;
import java.util.List;

/**
 *
 *
 */
@Form(fieldOrder = {"name", "base", "mimeType", "includes", "excludes", "postprocessors"})
@SymbolicName("zutubi.directoryArtifactConfig")
public class DirectoryArtifactConfiguration extends ArtifactConfiguration
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

    public DirectoryArtifactConfiguration()
    {
    }

    public DirectoryArtifactConfiguration(String name)
    {
        super(name);
    }

    public DirectoryArtifactConfiguration(String name, String base)
    {
        super(name);
        this.base = base;
    }

    public DirectoryArtifactConfiguration(String name, String base, String mimeType)
    {
        super(name);
        this.base = base;
        this.mimeType = mimeType;
    }

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

    @Transient
    public List<String> getIncludePatterns()
    {
        return splitPatterns(includes);
    }

    public String getExcludes()
    {
        return excludes;
    }

    public void setExcludes(String excludes)
    {
        this.excludes = excludes;
    }

    @Transient
    public List<String> getExcludePatterns()
    {
        return splitPatterns(excludes);
    }

    private List<String> splitPatterns(String patterns)
    {
        if(TextUtils.stringSet(patterns))
        {
            return StringUtils.split(patterns);
        }
        else
        {
            return Collections.emptyList();
        }
    }

    public String getMimeType()
    {
        return mimeType;
    }

    public void setMimeType(String mimeType)
    {
        this.mimeType = mimeType;
    }

    public String toString()
    {
        return base;
    }

    @Transient
    public String getType()
    {
        return "dir";
    }
}
