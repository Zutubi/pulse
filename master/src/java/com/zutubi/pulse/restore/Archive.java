package com.zutubi.pulse.restore;

import java.io.File;

/**
 *
 *
 */
public class Archive
{
    private File base;

    private String version = "N/A";
    private String date = "N/A";

    public Archive(File base)
    {
        this.base = base;

        loadManifest();
    }

    private void loadManifest()
    {
        
    }

    public String getVersion()
    {
        return version;
    }

    public String getDate()
    {
        return date;
    }
}
