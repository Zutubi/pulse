package com.zutubi.pulse.model;

import com.opensymphony.util.TextUtils;

/**
 * A capture for a single file.
 */
public class FileCapture extends Capture
{
    /**
     * Path of the file to capture, relative to the base directory.
     */
    private String file;
    /**
     * MIME type of the file, may be null in which case it will be guessed
     * when the user downloads the artifact.
     */
    private String mimeType;

    private FileCapture()
    {
        super(null);
    }

    public FileCapture(String name, String file)
    {
        this(name, file, null);
    }

    public FileCapture(String name, String file, String mimeType)
    {
        super(name);
        this.file = file;
        this.mimeType = mimeType;
    }

    public FileCapture copy()
    {
        FileCapture copy = new FileCapture();
        copyCommon(copy);
        copy.file = file;
        copy.mimeType = mimeType;
        return copy;
    }

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

    public String getType()
    {
        return "file";
    }

    public void clearFields()
    {
        if(!TextUtils.stringSet(mimeType))
        {
            mimeType = null;
        }
    }
}
