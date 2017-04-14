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

package com.zutubi.tove.events;

import com.zutubi.events.Event;
import com.zutubi.events.EventListener;

/**
 * A base listener implementation for configuration system events.  
 */
public abstract class ConfigurationSystemEventListener implements EventListener
{
    public Class[] getHandledEvents()
    {
        return new Class[]{ConfigurationSystemEvent.class};
    }

    public void handleEvent(Event event)
    {
        if (event instanceof ConfigurationEventSystemStartedEvent)
        {
            configurationEventSystemStarted();
        }
        else if (event instanceof ConfigurationSystemStartedEvent)
        {
            configurationSystemStarted();
        }

        configurationSystemEvent();
    }

    public void configurationEventSystemStarted()
    {

    }

    public void configurationSystemStarted()
    {
        
    }

    public void configurationSystemEvent()
    {

    }
}
