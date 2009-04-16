package com.zutubi.pulse.master.restore;

import java.io.File;

/**
 * Helper base for implementing archivable components.  Defines defaults where
 * possible.
 */
public abstract class AbstractArchiveableComponent implements ArchiveableComponent
{
    public boolean exists(File dir)
    {
        return true;
    }
}
