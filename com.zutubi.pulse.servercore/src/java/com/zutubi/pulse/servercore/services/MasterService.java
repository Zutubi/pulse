package com.zutubi.pulse.servercore.services;

import com.zutubi.events.Event;
import com.zutubi.pulse.core.config.ResourceConfiguration;
import com.zutubi.pulse.core.plugins.repository.PluginInfo;

import java.util.List;

/**
 * Interface used by hosts/agents to communicate with a Pulse master.
 */
public interface MasterService
{
    /**
     * Used to test communication in the host->master direction.  As an
     * optimisation, the pong also returns a list of all plugins running on
     * this master (to save an extra round trip).
     * 
     * @return a list of all plugins running on this master
     */
    List<PluginInfo> pong();

    void upgradeStatus(String token, UpgradeStatus upgradeStatus);

    void handleEvent(String token, Event event) throws InvalidTokenException;

    ResourceConfiguration getResource(String token, long handle, String name) throws InvalidTokenException;
}
