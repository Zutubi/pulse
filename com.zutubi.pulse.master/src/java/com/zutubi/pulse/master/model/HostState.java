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

package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.master.agent.HostLocationFormatter;

/**
 * Stores persistent state for a host.  Hosts may be shared by multiple agents.
 */
public class HostState extends Entity
{
    public enum PersistentUpgradeState
    {
        /**
         * The usual state: no upgrade in progress.
         */
        NONE,
        /**
         * An upgrade is in progress.  Stored persistently in case the master
         * goes down during agent upgrade.
         */
        UPGRADING,
        /**
         * An upgrade failed.  The host remains in this state until the user
         * intervenes.
         */
        FAILED_UPGRADE
    }

    private boolean remote;
    private String hostName;
    private int port;
    private boolean ssl;
    private PersistentUpgradeState persistentUpgradeState = PersistentUpgradeState.NONE;

    public HostState()
    {
        remote = false;
        hostName = HostLocationFormatter.LOCATION_MASTER;
    }

    public HostState(String hostName, int port, boolean ssl)
    {
        remote = true;
        this.hostName = hostName;
        this.port = port;
        this.ssl = ssl;
    }

    public boolean isRemote()
    {
        return remote;
    }

    public void setRemote(boolean remote)
    {
        this.remote = remote;
    }

    public String getHostName()
    {
        return hostName;
    }

    public void setHostName(String hostName)
    {
        this.hostName = hostName;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public boolean isSsl()
    {
        return ssl;
    }

    public void setSsl(boolean ssl)
    {
        this.ssl = ssl;
    }

    public PersistentUpgradeState getUpgradeState()
    {
        return persistentUpgradeState;
    }

    public void setUpgradeState(PersistentUpgradeState persistentUpgradeState)
    {
        this.persistentUpgradeState = persistentUpgradeState;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private String getPersistentUpgradeStateName()
    {
        return persistentUpgradeState.name();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private void setPersistentUpgradeStateName(String persistentUpgradeStateName)
    {
        this.persistentUpgradeState = PersistentUpgradeState.valueOf(persistentUpgradeStateName);
    }
}
