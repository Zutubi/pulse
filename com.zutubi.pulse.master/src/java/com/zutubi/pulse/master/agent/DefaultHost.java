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

import com.zutubi.pulse.master.model.HostState;
import com.zutubi.pulse.servercore.services.UpgradeState;

/**
 * Default implementation of {@link Host}.
 */
public class DefaultHost implements Host, HostLocation
{
    /**
     * This is a detached entity that should be treated as read-only and not
     * accessible to the outside world.
     */
    private HostState state;
    /**
     * The upgrade state is only used when the persistent upgrade state is UPGRADING.
     */
    private UpgradeState upgradeState = UpgradeState.INITIAL;
    private int upgradeProgress = -1;
    private String upgradeMessage = null;

    public DefaultHost(HostState state)
    {
        this.state = state;
    }

    public long getId()
    {
        return state.getId();
    }

    public boolean isRemote()
    {
        return state.isRemote();
    }

    public String getHostName()
    {
        return state.getHostName();
    }

    public int getPort()
    {
        return state.getPort();
    }

    public boolean isSsl()
    {
        return state.isSsl();
    }

    public String getLocation()
    {
        return HostLocationFormatter.format(this);
    }

    public HostState.PersistentUpgradeState getPersistentUpgradeState()
    {
        return state.getUpgradeState();
    }

    public boolean isUpgrading()
    {
        return state.getUpgradeState() != HostState.PersistentUpgradeState.NONE;
    }

    public UpgradeState getUpgradeState()
    {
        return upgradeState;
    }

    public int getUpgradeProgress()
    {
        return upgradeProgress;
    }

    public String getUpgradeMessage()
    {
        return upgradeMessage;
    }

    synchronized void upgradeStatus(UpgradeState state, int progress, String message)
    {
        upgradeState = state;
        upgradeProgress = progress;
        upgradeMessage = message;
    }

    synchronized void setState(HostState hostState)
    {
        state = hostState;
    }
}
