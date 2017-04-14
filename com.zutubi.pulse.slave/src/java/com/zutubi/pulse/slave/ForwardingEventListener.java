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

package com.zutubi.pulse.slave;

import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.events.RecipeEvent;
import com.zutubi.pulse.core.events.SlaveCommandEvent;
import com.zutubi.pulse.servercore.events.SynchronisationMessageProcessedEvent;
import com.zutubi.pulse.servercore.services.MasterService;
import com.zutubi.pulse.servercore.services.ServiceTokenManager;
import com.zutubi.util.logging.Logger;

/**
 * An event listener that forwards relevant events to the master server.
 */
public class ForwardingEventListener implements EventListener
{
    private static final Logger LOG = Logger.getLogger(ForwardingEventListener.class);

    private String masterUrl;
    private MasterService masterService;
    private ServiceTokenManager serviceTokenManager;

    public synchronized void setMaster(String masterUrl, MasterService masterService)
    {
        this.masterUrl = masterUrl;
        this.masterService = masterService;
    }
    
    public void handleEvent(Event event)
    {
        String masterUrl;
        MasterService masterService;
        synchronized (this)
        {
            masterUrl = this.masterUrl;
            masterService = this.masterService;
        }
        
        if (masterService != null)
        {
            try
            {
                masterService.handleEvent(serviceTokenManager.getToken(), event);
            }
            catch (Exception e)
            {
                // TODO abort the recipe execution?
                // TODO support retrying events
                LOG.severe("Could not forward event to master.  Check master location (" + masterUrl + ") is accessible from this agent", e);
            }
        }
        else
        {
            LOG.severe("Could not forward event to master: no master registered");
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{ SlaveCommandEvent.class, RecipeEvent.class, SynchronisationMessageProcessedEvent.class};
    }

    public void setEventManager(EventManager eventManager)
    {
        eventManager.register(this);
    }
    
    public void setServiceTokenManager(ServiceTokenManager serviceTokenManager)
    {
        this.serviceTokenManager = serviceTokenManager;
    }
}
