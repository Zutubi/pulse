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

    private File original;

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

    public File getOriginal()
    {
        return original;
    }

    public void setOriginal(File original)
    {
        this.original = original;
    }
}
