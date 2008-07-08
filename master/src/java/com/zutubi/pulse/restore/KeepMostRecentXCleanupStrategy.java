package com.zutubi.pulse.restore;

import java.io.File;
import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 *
 *
 */
public class KeepMostRecentXCleanupStrategy implements BackupCleanupStrategy
{
    private int x = 10;

    public KeepMostRecentXCleanupStrategy()
    {
    }

    public KeepMostRecentXCleanupStrategy(int x)
    {
        this.x = x;
    }

    public File[] getCleanupTargets(File[] cleanupCandidates)
    {
        if (cleanupCandidates == null || cleanupCandidates.length <= x)
        {
            return new File[0];
        }

        List<File> chronologicalListing = new LinkedList<File>(Arrays.asList(cleanupCandidates));
        Collections.sort(chronologicalListing, new Comparator<File>()
        {
            public int compare(File o1, File o2)
            {
                return o1.lastModified() < o2.lastModified() ? -1 : 1;
            }
        });

        int cleanupCount = chronologicalListing.size() - x;
        return chronologicalListing.subList(0, cleanupCount).toArray(new File[cleanupCount]);
    }
}
