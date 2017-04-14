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

package com.zutubi.pulse.master.agent;

/**
 * Utility class to format host locations.
 */
public class HostLocationFormatter
{
    public static final String LOCATION_MASTER = "[master]";

    /**
     * Formats a location into a single string which is human-readable and
     * unique for different locations.
     *
     * @param location the location to format
     * @return the formatted location
     */
    public static String format(HostLocation location)
    {
        if (location.isRemote())
        {
            return (location.isSsl() ? "https" : "http") + "://" + (location.getHostName() == null ? "" : location.getHostName()) + ":" + location.getPort();
        }
        else
        {
            return LOCATION_MASTER;
        }
    }
}
