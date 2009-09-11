package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.model.Entity;

/**
 * Stores persistent state for a host.  Hosts may be shared by multiple agents.
 */
public class HostState extends Entity
{
    public static final String LOCATION_MASTER = "[master]";

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
    private PersistentUpgradeState persistentUpgradeState = PersistentUpgradeState.NONE;

    public HostState()
    {
        remote = false;
        hostName = LOCATION_MASTER;
    }

    public HostState(String hostName, int port)
    {
        remote = true;
        this.hostName = hostName;
        this.port = port;
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
