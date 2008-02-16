package com.zutubi.pulse.restore;

import java.io.File;
import java.io.IOException;

/**
 *
 *
 */
public class Archive
{
    private File base;

    private ArchiveManifest manifest;

    public Archive(File base, ArchiveManifest manifest)
    {
        this.base = base;
        this.manifest = manifest;
    }

    public ArchiveManifest getManifest()
    {
        return manifest;
    }

    public File getFile()
    {
        return base;
    }

    public String getVersion()
    {
        return manifest.getVersion();
    }

    public String getCreated()
    {
        return manifest.getCreated();
    }

    public String getAuthor()
    {
        return manifest.getAuthor();
    }
}
