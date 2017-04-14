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

package com.zutubi.pulse.servercore.agent;

import com.zutubi.util.EnumUtils;

/**
 * Basic state of a host returned by a ping.
 */
public enum PingStatus
{
    /**
     * The host cannot be contacted.
     */
    OFFLINE(false),
    /**
     * The host is reporting a different build number to the master.
     */
    VERSION_MISMATCH(false),
    /**
     * The host either has no plugins (new install) or has different plugins
     * that require a reboot to get in sync with the master.
     */
    PLUGIN_MISMATCH(false),
    /**
     * The master's authentication token does not match the host's.
     */
    TOKEN_MISMATCH(false),
    /**
     * The master can contact the host, but the host cannot contact the master.
     */
    INVALID_MASTER(false),
    /**
     * One or more agents on the host is running a build. 
     */
    BUILDING(true),
    /**
     * The agents on the host are all idle (not running a build).
     */
    IDLE(true);

    private boolean online;

    PingStatus(boolean online)
    {
        this.online = online;
    }

    /**
     * Returns a human-readable version of this state.
     *
     * @return a pretty version of this state
     */
    public String getPrettyString()
    {
        return EnumUtils.toPrettyString(this);
    }

    /**
     * Indicates if this is an online state.
     *
     * @return true if this state implies the agent is online (able to build,
     *         although possibly busy)
     */
    public boolean isOnline()
    {
        return online;
    }
}
