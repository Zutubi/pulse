/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.restore;

import java.io.File;
import java.util.*;

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
                return o1.lastModified() < o2.lastModified() ? -1 : o1.lastModified() == o2.lastModified() ? 0 : 1;
            }
        });

        int cleanupCount = chronologicalListing.size() - x;
        return chronologicalListing.subList(0, cleanupCount).toArray(new File[cleanupCount]);
    }
}
