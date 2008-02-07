package com.zutubi.pulse.restore;

import com.zutubi.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 *
 *
 */
public class ArchiveManifest
{
    public static final String CREATED = "created";
    public static final String VERSION = "version";
    public static final String AUTHOR = "author";

    private String created;
    private String version;
    private String author;

    protected ArchiveManifest(String created, String version, String author)
    {
        this.created = created;
        this.version = version;
        this.author = author;
    }

    public String getCreated()
    {
        return created;
    }

    public String getVersion()
    {
        return version;
    }

    public String getAuthor()
    {
        return author;
    }

    protected void writeTo(File f) throws IOException
    {
        Properties properties = new Properties();
        properties.put(CREATED, created);
        properties.put(VERSION, version);
        properties.put(AUTHOR, author);
        IOUtils.write(properties, f);
    }

    protected static ArchiveManifest readFrom(File f) throws IOException
    {
        Properties properties = IOUtils.read(f);
        return new ArchiveManifest(
                properties.getProperty(CREATED),
                properties.getProperty(VERSION),
                properties.getProperty(AUTHOR)
        );
    }
}
