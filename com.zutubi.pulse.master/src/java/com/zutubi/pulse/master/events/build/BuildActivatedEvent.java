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

package com.zutubi.pulse.master.events.build;

/**
 * This event is raised when a build is activated: this is the point where
 * the build controller is about to start and enqueue the recipe requests.
 */
public class BuildActivatedEvent extends BuildEvent
{
    private BuildRequestEvent event;

    public BuildActivatedEvent(Object source, BuildRequestEvent event)
    {
        super(source, null, null);
        this.event = event;
    }

    public BuildRequestEvent getEvent()
    {
        return event;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        BuildActivatedEvent event1 = (BuildActivatedEvent) o;
        return !(event != null ? !event.equals(event1.event) : event1.event != null);
    }

    public int hashCode()
    {
        return (event != null ? event.hashCode() : 0);
    }

    public String toString()
    {
        return "Build Activated Event: " + getBuildResult();
    }
}
