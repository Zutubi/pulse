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

package com.zutubi.pulse.master.events;

import com.zutubi.pulse.master.agent.Host;
import com.zutubi.pulse.servercore.services.HostStatus;

/**
 * Event raised when a host ping completes.
 */
public class HostPingEvent extends HostEvent
{
    private HostStatus hostStatus;

    public HostPingEvent(Object source, Host host, HostStatus hostStatus)
    {
        super(source, host);
        this.hostStatus = hostStatus;
    }

    public HostStatus getHostStatus()
    {
        return hostStatus;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder("Host Ping Event");
        if (getHost() != null)
        {
            builder.append(": ").append(getHost().getLocation()).append(", ").append(hostStatus);
        }
        return builder.toString();
    }
}
