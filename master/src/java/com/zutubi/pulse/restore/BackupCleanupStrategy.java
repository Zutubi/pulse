package com.zutubi.pulse.restore;

import java.io.File;

/**
 *
 *
 */
public interface BackupCleanupStrategy
{
    File[] getCleanupTargets(File[] cleanupCandidates);
}
