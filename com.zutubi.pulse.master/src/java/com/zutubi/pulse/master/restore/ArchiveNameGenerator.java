package com.zutubi.pulse.master.restore;

import java.io.File;

/**
 *
 *
 */
public interface ArchiveNameGenerator
{
    String newName(File target);
}
