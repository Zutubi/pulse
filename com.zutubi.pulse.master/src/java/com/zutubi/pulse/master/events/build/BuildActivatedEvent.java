package com.zutubi.pulse.master.events.build;

import com.zutubi.pulse.master.model.User;

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
        StringBuilder builder = new StringBuilder("Build Activated Event:");
        if (event.isPersonal())
        {
            builder.append(" Personal Build: ");
            builder.append(((User) event.getOwner()).getLogin());
        }

        builder.append(" Project: ");
        builder.append(event.getProjectConfig().getName());
        return builder.toString();
    }
}
