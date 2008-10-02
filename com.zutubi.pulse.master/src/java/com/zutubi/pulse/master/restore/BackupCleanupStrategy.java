package com.zutubi.pulse.master.restore;

import java.io.File;

/**
 *
 *
 */
public interface BackupCleanupStrategy
{
    File[] getCleanupTargets(File[] cleanupCandidates);
}
