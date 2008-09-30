package com.zutubi.pulse.restore;

import java.io.File;

/**
 *
 *
 */
public interface ArchiveNameGenerator
{
    String newName(File target);
}
