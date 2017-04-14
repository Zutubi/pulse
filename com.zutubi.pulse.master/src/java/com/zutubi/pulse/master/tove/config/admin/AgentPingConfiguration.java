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

package com.zutubi.pulse.master.tove.config.admin;

import com.zutubi.tove.annotations.Classification;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.validation.annotations.Min;

/**
 */
@SymbolicName("zutubi.agentPingConfig")
@Form(fieldOrder={"pingInterval", "pingTimeout", "offlineTimeout", "timeoutLoggingEnabled"})
@Classification(single = "ping")
public class AgentPingConfiguration extends AbstractConfiguration
{
    @Min(1)
    private int maxConcurrent = 12;
    @Min(1)
    private int pingInterval = 60;
    @Min(1)
    private int pingTimeout = 45;
    @Min(1)
    private int offlineTimeout = 240;
    private boolean timeoutLoggingEnabled = true;

    public AgentPingConfiguration()
    {
        setPermanent(true);
    }

    public int getMaxConcurrent()
    {
        return maxConcurrent;
    }

    public void setMaxConcurrent(int maxConcurrent)
    {
        this.maxConcurrent = maxConcurrent;
    }

    public int getPingInterval()
    {
        return pingInterval;
    }

    public void setPingInterval(int pingInterval)
    {
        this.pingInterval = pingInterval;
    }

    public int getPingTimeout()
    {
        return pingTimeout;
    }

    public void setPingTimeout(int pingTimeout)
    {
        this.pingTimeout = pingTimeout;
    }

    public int getOfflineTimeout()
    {
        return offlineTimeout;
    }

    public void setOfflineTimeout(int offlineTimeout)
    {
        this.offlineTimeout = offlineTimeout;
    }

    public boolean isTimeoutLoggingEnabled()
    {
        return timeoutLoggingEnabled;
    }

    public void setTimeoutLoggingEnabled(boolean timeoutLoggingEnabled)
    {
        this.timeoutLoggingEnabled = timeoutLoggingEnabled;
    }
}
