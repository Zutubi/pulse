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

package com.zutubi.pulse.core.dependency.ivy;

import java.util.List;
import java.util.Arrays;

/**
 * The ivy status object provides local access to the default status configuration used
 * within ivy.  We currently have no plans to allow the status's to be configurable, so
 * there is no need to use ivy's status configurations when a local version available.
 *
 * This has the added benefit of allowing us to avoid loading ivy classes ahead of time.
 */
public class IvyStatus
{
    // These status's are the default status's configured in ivy.  If / when we allow
    // customisation of the status's, these static fields will need to be revisited.
    // See org.apache.ivy.core.module.status.StatusManager for details.
    public static String STATUS_INTEGRATION   = "integration";
    public static String STATUS_MILESTONE     = "milestone";
    public static String STATUS_RELEASE       = "release";

    /**
     * Get the list of available build statuses.
     *
     * @return the list of build statuses.
     */
    public static List<String> getStatuses()
    {
        return Arrays.asList(STATUS_RELEASE, STATUS_MILESTONE, STATUS_INTEGRATION);
    }

    /**
     * Get the priority of the status.  The priority represents an ordering that allows interaction
     * between the different status's.  Take 'integration' and 'release' for example.  'release' has a
     * priority of 1, 'integration' or 3.  The priority of 3 for integration means that it will accept
     * any artifact of a status lower than or equal to itself.  So, an integration build will happily
     * accept release builds.  However, a release build will not accept an integration build.
     *
     * @param status    the status string.
     * @return  a number that represents priority of the status, as explained above.
     */
    public static int getPriority(String status)
    {
        if (getStatuses().contains(status))
        {
            return getStatuses().indexOf(status);
        }
        return Integer.MAX_VALUE;
    }

    /**
     * Get the default build status
     *
     * @return the default build status.
     */
    public static String getDefaultStatus()
    {
        return STATUS_INTEGRATION;
    }

    /**
     * Returns true if the specified status string is a valid
     * status string.
     *
     * @param status    the string being tested.
     *
     * @return true if the status string is one of the valid status's
     *
     * @see #getStatuses()
     */
    public static boolean isValidStatus(String status)
    {
        return getStatuses().contains(status);
    }
}
