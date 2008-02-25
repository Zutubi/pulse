package com.zutubi.pulse.restore;

import java.io.File;

/**
 *
 *
 */
public class Archive
{
    /**
     * The base (expanded directory) for this imported archive.
     */
    private File base;

    /**
     * If specified, this file is a reference to the original archive file, the file that was imported.
     *
     * @see ArchiveFactory#importArchive(java.io.File) 
     */
    private File original;

    /**
     * This archives manifest.
     */
    private ArchiveManifest manifest;

    public Archive(File original, File base, ArchiveManifest manifest)
    {
        this.base = base;
        this.manifest = manifest;
        this.original = original;
    }

    public Archive(File base, ArchiveManifest manifest)
    {
        this(null, base, manifest);
    }

    public ArchiveManifest getManifest()
    {
        return manifest;
    }

    public File getBase()
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

    public File getOriginal()
    {
        return original;
    }
}
