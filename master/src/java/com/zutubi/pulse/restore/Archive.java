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

    private void loadManifest()
    {
        // manifest.txt, located in the root directory.
        File manifestFile = new File(base, "manifest.txt");
        if (!manifestFile.isFile())
        {
            // does this make this an invalid archive file?... probably.
        }

        try
        {
            manifest = ArchiveManifest.readFrom(manifestFile);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            // this is also likely to cause problems - invalid archive here...
        }
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
