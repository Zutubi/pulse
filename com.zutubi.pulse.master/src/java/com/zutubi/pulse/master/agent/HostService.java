package com.zutubi.pulse.master.agent;

import com.zutubi.pulse.core.config.ResourceConfiguration;
import com.zutubi.pulse.servercore.SystemInfo;
import com.zutubi.pulse.servercore.services.HostStatus;
import com.zutubi.pulse.servercore.util.logging.CustomLogRecord;

import java.util.List;

/**
 * Service for communication with hosts.
 */
public interface HostService
{
    /**
     * Ping the host, to check connectivity nd compatibility.
     *
     * @return the build number of the host
     */
    int ping();

    /**
     * Returns the status of the agent, with extra connectivity information and
     * details of any builds it is running.
     *
     * @param masterLocation the location of this master, so the host can call
     *                       us back (verified by the host in this call)
     * @return the current status of the host
     */
    HostStatus getStatus(String masterLocation);

    /**
     * Returns information about the machine the host is on.
     *
     * @return system information for the host machine
     */
    SystemInfo getSystemInfo();

    /**
     * Returns the most recent error and warning messages for the host.  The
     * messages are transient (i.e. immediately after a host restart this
     * method will return an empty list).
     *
     * @return recent errors and warnings on the host
     */
    List<CustomLogRecord> getRecentMessages();

    /**
     * Suggests that now might be a nice time to run garbage collection to the
     * host server process.
     */
    void garbageCollect();

    /**
     * Discovers commonly-recognised resources on the host, and returns their
     * details.
     *
     * @return details of resources discovered on the host
     */
    List<ResourceConfiguration> discoverResources();

    /**
     * Requests that the host try to upgrade to this master's version
     * automatically.
     *
     * @param masterBuild the build number of this master
     * @param masterUrl   the url the host can reach this master on
     * @param hostId      id of the host, passed back in status updates
     * @param packageUrl  url of an agent package to upgrade to
     * @param packageSize size of the agent package in bytes
     * @return true if the host will attempt the upgrade
     */
    boolean updateVersion(String masterBuild, String masterUrl, long hostId, String packageUrl, long packageSize);

    /**
     * Requests that the host synchronise its plugins with the master if
     * required.
     * 
     * @param masterUrl           the url the host can reach this master on
     * @param hostId              id of the host, passed back in status updates
     * @param pluginRepositoryUrl the url of the repository to synchronise with
     * @return true if the plugins need to be synchronised (the host will begin
     *         synchronising them), false if they are already in sync.
     */
    boolean syncPlugins(String masterUrl, long hostId, String pluginRepositoryUrl);
}
