package com.zutubi.pulse.master.agent;

import com.zutubi.pulse.master.model.HostState;
import com.zutubi.pulse.servercore.services.UpgradeState;

/**
 * A host is a machine that is capabale of hosting agents.  Multiple agents
 * that have the same location will share a single host.
 */
public interface Host
{
    /**
     * Returns the id of the host state entity.
     *
     * @return a unique id for this host
     */
    long getId();

    /**
     * Indicates if this agent is remote (on a slave server) or local (in the
     * master).
     *
     * @return true if the host is on a remote machine, false if it is on the
     *         master
     */
    boolean isRemote();

    /**
     * The host name of the machine the host is on (may be "localhost" for the
     * master).
     *
     * @return the host name of the machine this host is on
     */
    String getHostName();

    /**
     * The port that the agent server is listening on, only valid for remote
     * hosts.
     *
     * @return the port the agent server is listening on
     */
    int getPort();

    /**
     * The location of the host, which is unique for each host and human-
     * readable.
     *
     * @return the location of this host
     */
    String getLocation();

    /**
     * Indicates the high-level, persistent upgrade state of the host.  This
     * state survives master reboots.
     *
     * @return the persistent upgrade state of this host
     */
    HostState.PersistentUpgradeState getPersistentUpgradeState();

    /**
     * Indicates if this host is in an upgrading state, including when an
     * upgrade has failed.
     *
     * @return true if the host is trying to or has failed to upgrade
     */
    boolean isUpgrading();

    /**
     * Indicates the transient upgrade state of the host, which is more
     * detailed than the persistent state but does not survive master reboots.
     *
     * @return the current upgrade state of the host, only valid when the
     *         persistent upgrade state is {@link com.zutubi.pulse.master.model.HostState.PersistentUpgradeState#UPGRADING}.
     */
    UpgradeState getUpgradeState();

    /**
     * Indicates a percentage progress (0-100) for current the upgrade step,
     * while the host is upgrading.  Zero means there is no progress.
     *
     * @return the percentage of progress of the current upgrade step, only
     *         valid when the persistent upgrade state is
     *         {@link com.zutubi.pulse.master.model.HostState.PersistentUpgradeState#UPGRADING}
     */
    int getUpgradeProgress();

    /**
     * An optional upgrade message, indicating what went wrong when an upgrade
     * failed.
     *
     * @return an upgrade error message providing more details when the
     *         persistent upgrade state is {@link com.zutubi.pulse.master.model.HostState.PersistentUpgradeState#FAILED_UPGRADE},
     *         may be null when no details are available
     */
    String getUpgradeMessage();
}
