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

package com.zutubi.pulse.servercore.services;

import com.zutubi.events.Event;
import com.zutubi.pulse.core.plugins.repository.PluginInfo;
import com.zutubi.pulse.core.resources.api.ResourceConfiguration;

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
