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

package com.zutubi.pulse.servercore.events.system;

import com.zutubi.events.EventListener;
import com.zutubi.events.Event;

/**
 * A utility listener to simplify the registration of an event listener
 * to listen for the system started event.
 */
public abstract class SystemStartedListener implements EventListener
{
    public Class[] getHandledEvents()
    {
        return new Class[]{SystemStartedEvent.class};
    }

    public void handleEvent(Event event)
    {
        systemStarted();
    }

    public abstract void systemStarted();
}
