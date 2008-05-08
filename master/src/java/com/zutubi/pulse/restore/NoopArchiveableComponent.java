package com.zutubi.pulse.restore;

import java.io.File;

/**
 *
 *
 */
public class NoopArchiveableComponent extends AbstractArchiveableComponent
{
    public String getName()
    {
        return "noop";
    }

    public String getDescription()
    {
        return "noop";
    }

    public void backup(File archive) throws ArchiveException
    {

    }

    public void restore(File archive) throws ArchiveException
    {

    }
}