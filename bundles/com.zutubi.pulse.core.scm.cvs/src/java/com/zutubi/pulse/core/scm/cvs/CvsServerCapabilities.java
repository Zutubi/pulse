package com.zutubi.pulse.core.scm.cvs;

import java.util.Comparator;

/**
 * A helper class that knows which versions of the cvs server implementation
 * support certain functions.
 */
public class CvsServerCapabilities
{
    private static final VersionComparator COMPARATOR = new VersionComparator();

    /**
     * Returns true if the specified version of the cvs server supports
     * running the rls command.
     *
     * @param version   cvs server version
     * @return true if rls is supported, false otherwise.
     */
    public static boolean supportsRemoteListing(String version)
    {
        return COMPARATOR.compare("1.12.0", version) > 0;
    }

    /**
     * Returns true if the specified version of the cvs server supports updates
     * to a specific date on a cvs branch.
     *
     * @param version   cvs server version
     * 
     * @return true if we can update to a specific date on the branch.
     */
    public static boolean supportsDateRevisionOnBranch(String version)
    {
        return COMPARATOR.compare("1.12.12", version) >= 0;
    }

    private static class VersionComparator implements Comparator<String>
    {
        public int compare(String o1, String o2)
        {
            // Special case: 1.11.1p1, just treat it as 1.11.1 for now. There are
            // numerous versions of the 1.11.1p1 series all presumably based on the 1.11.1
            // featureset.
            if (o2.startsWith("1.11.1p1"))
            {
                o2 = "1.11.1";
            }

            String[] v1 = o1.split("\\.");
            String[] v2 = o2.split("\\.");

            for (int i = 0; i < v1.length && i < v2.length; i++)
            {
                String c1 = v1[i];
                String c2 = v2[i];

                int i1 = Integer.parseInt(c1);
                int i2 = Integer.parseInt(c2);

                if (i1 != i2)
                {
                    return i2 - i1;
                }
            }

            if (v1.length == v2.length)
            {
                return 0;
            }

            if (v1.length > v2.length)
            {
                return 0 - Integer.parseInt(v1[v2.length]);
            }
            else
            {
                return Integer.parseInt(v2[v1.length]);
            }
        }
    }
}
