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

package com.zutubi.pulse.core.scm.cvs;

import java.util.Comparator;

/**
 * A helper class that knows which versions of the cvs server implementation
 * support certain functions.
 */
public class CvsServerCapabilities
{
    static final String SUPPORT_REMOTE_LISTING = "cvs.rls.supported";
    static final String SUPPORT_DATE_REVISION_ON_BRANCH = "cvs.branch.revisions.supported";
    static final String SUPPORT_RLOG_SUPPRESS_HEADER = "cvs.rlog.suppressHeader";

    private static final VersionComparator COMPARATOR = new VersionComparator();

    public static boolean supportsRlogSuppressHeader()
    {
        if (System.getProperties().containsKey(SUPPORT_RLOG_SUPPRESS_HEADER))
        {
            return Boolean.valueOf(SUPPORT_RLOG_SUPPRESS_HEADER);
        }
        return true;
    }

    /**
     * Returns true if the specified version of the cvs server supports
     * running the rls command.
     *
     * @param version   cvs server version
     * @return true if rls is supported, false otherwise.
     */
    public static boolean supportsRemoteListing(String version)
    {
        if (System.getProperties().containsKey(SUPPORT_REMOTE_LISTING))
        {
            return Boolean.valueOf(SUPPORT_REMOTE_LISTING);
        }
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
        if (System.getProperties().containsKey(SUPPORT_DATE_REVISION_ON_BRANCH))
        {
            return Boolean.valueOf(SUPPORT_DATE_REVISION_ON_BRANCH);
        }
        return COMPARATOR.compare("1.12.12", version) >= 0;
    }

    private static class VersionComparator implements Comparator<String>
    {
        public int compare(String o1, String o2)
        {
            // Special case: 1.11.1p1, just treat it as 1.11.1 for now. There are
            // numerous versions of the 1.11.1p1 series all presumably based on the 1.11.1
            // featureset.
            try
            {
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
            catch (Exception e)
            {
                // if we are dealing with a revision of unexpected format, fall back
                // to not supporting the feature.  In this case the use can override
                // this default behaviour with custom properties.
                return -1;
            }
        }
    }
}
