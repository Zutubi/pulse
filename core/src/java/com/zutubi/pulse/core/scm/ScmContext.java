package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.model.Revision;

import java.io.File;
import java.util.Properties;

/**
 *
 *
 */
public class ScmContext
{
    private String id;

    private Revision revision;

    private File dir;

    private Properties props = new Properties();

    public Revision getRevision()
    {
        return revision;
    }

    public void setRevision(Revision revision)
    {
        this.revision = revision;
    }

    public File getDir()
    {
        return dir;
    }

    public void setDir(File dir)
    {
        this.dir = dir;
    }

    public void addProperty(String key, String value, boolean flagA, boolean flagB, boolean flagC)
    {
        props.put(key, value);
    }

    public Properties getProperties()
    {
        return props;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }
}
