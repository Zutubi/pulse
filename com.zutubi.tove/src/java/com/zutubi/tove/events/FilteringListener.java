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

import com.google.common.base.Predicate;
import com.zutubi.events.Event;
import com.zutubi.events.EventListener;

/**
 * A listener that filters events before passing them on to a delegate,
 * allowing selection of events based on more than just the event class.
 */
public class FilteringListener implements EventListener
{
    private Predicate<Event> predicate;
    private EventListener delegate;

    public FilteringListener(Predicate<Event> predicate, EventListener delegate)
    {
        this.predicate = predicate;
        this.delegate = delegate;
    }

    public EventListener getDelegate()
    {
        return delegate;
    }

    public void handleEvent(Event evt)
    {
        if(predicate.apply(evt))
        {
            delegate.handleEvent(evt);
        }
    }

    public Class[] getHandledEvents()
    {
        return delegate.getHandledEvents();
    }
}
