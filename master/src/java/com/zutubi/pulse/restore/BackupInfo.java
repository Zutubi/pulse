package com.zutubi.pulse.restore;

import java.io.File;

/**
 * Manifest type information extracted from the backup.
 *
 */
public class BackupInfo
{
    private String version;
    private String date;
    private File source;

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public String getDate()
    {
        return date;
    }

    public void setDate(String date)
    {
        this.date = date;
    }

    public File getSource()
    {
        return source;
    }

    public void setSource(File source)
    {
        this.source = source;
    }
}
