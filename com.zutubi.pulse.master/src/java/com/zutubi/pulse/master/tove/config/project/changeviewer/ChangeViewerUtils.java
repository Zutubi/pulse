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

package com.zutubi.pulse.master.tove.config.project.changeviewer;

import com.zutubi.pulse.core.scm.api.Revision;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper methods for the implementation of change viewers.
 */
public class ChangeViewerUtils
{
    public static final String PROPERTY_AUTHOR = "author";
    public static final String PROPERTY_BRANCH = "branch";
    public static final String PROPERTY_DATE   = "date";

    /**
     * Pulls potentially-interesting properties out of a revision string.  If
     * the revision string appears to be from CVS, properties for the author,
     * branch and date are extracted.
     *
     * @param revision revision to analyse and extrat properties from
     * @return properties found in the reivison string, empty if it does not
     *         appear to be a CVS revision
     */
    public static Map<String, Object> getRevisionProperties(Revision revision)
    {
        Map<String, Object> result = new HashMap<String, Object>(5);

        String revStr = revision.getRevisionString();

        // Interesting revisions have layout:
        //     <author>:<branch/tag>:<date>
        if (revStr.indexOf(":") == -1 || revStr.substring(revStr.indexOf(":")).indexOf(":") == -1)
        {
            return result;
        }

        String author = revStr.substring(0, revStr.indexOf(":"));
        String remainder = revStr.substring(revStr.indexOf(":") + 1);
        String branch = remainder.substring(0, remainder.indexOf(":"));
        String date = remainder.substring(remainder.indexOf(":") + 1);

        if (!author.equals(""))
        {
            result.put(PROPERTY_AUTHOR, author);
        }

        if (!branch.equals(""))
        {
            result.put(PROPERTY_BRANCH, branch);
        }

        if (!date.equals(""))
        {
            try
            {
                result.put(PROPERTY_DATE, getFullDateFormat().parse(date));
            }
            catch (ParseException e)
            {
                // noop.
            }
        }

        return result;
    }

    private static SimpleDateFormat getFullDateFormat()
    {
        return new SimpleDateFormat("yyyyMMdd-HH:mm:ss");
    }
}
