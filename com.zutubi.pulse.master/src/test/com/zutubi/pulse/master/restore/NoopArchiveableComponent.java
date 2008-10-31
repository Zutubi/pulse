package com.zutubi.pulse.master.restore;

import java.io.File;

/**
 * An implementation of the ArchiveableComponent that does nothing.
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
        // noop.
    }

    public void restore(File archive) throws ArchiveException
    {
        // noop.
    }
}