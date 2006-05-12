package com.zutubi.pulse.model;

import com.opensymphony.util.TextUtils;

import java.util.List;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * A capture for multi files nested under a directory.
 */
public class DirectoryCapture extends Capture
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

    private DirectoryCapture()
    {
        super(null);
    }

    public DirectoryCapture(String name)
    {
        this(name, null, null);
    }

    public DirectoryCapture(String name, String base)
    {
        this(name, base, null);
    }

    public DirectoryCapture(String name, String base, String mimeType)
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

    public String getType()
    {
        return "directory";
    }

    public void clearFields()
    {
        if(!TextUtils.stringSet(base))
        {
            base = null;
        }

        if(!TextUtils.stringSet(mimeType))
        {
            mimeType = null;
        }
    }

    public List<String> getIncludePatterns()
    {
        return getPatterns(getIncludes());
    }

    public List<String> getExcludePatterns()
    {
        return getPatterns(getExcludes());
    }

    private List<String> getPatterns(String cludes)
    {
        if(TextUtils.stringSet(cludes))
        {
            return Arrays.asList(cludes.split(" +"));
        }
        else
        {
            return new LinkedList<String>();
        }
    }
}
