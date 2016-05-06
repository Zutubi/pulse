package com.zutubi.pulse.master.logging;

import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.security.Actor;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.events.RecordDeletedEvent;
import com.zutubi.tove.type.record.events.RecordEvent;
import com.zutubi.tove.type.record.events.RecordInsertedEvent;
import com.zutubi.tove.type.record.events.RecordUpdatedEvent;
import com.zutubi.util.logging.Logger;

/**
 * Produces an audit log of changes to records.
 */
public class ConfigAuditLogger implements EventListener
{
    private static final Logger LOG = MasterLoggers.getConfigAuditLogger();

    private static final String USER_UNKNOWN = "<unknown>";
    private static final String USER_ANONYMOUS = "<anonymous>";

    private AccessManager accessManager;

    private void handleRecordInserted(RecordInsertedEvent event)
    {
        LOG.info(getUser() + ": inserted: " + event.getPath());
    }

    private void handleRecordDeleted(RecordDeletedEvent event)
    {
        LOG.info(getUser() + ": deleted: " + event.getPath());
    }

    private void handleRecordUpdated(RecordUpdatedEvent event)
    {
        LOG.info(getUser() + ": updated: " + event.getPath() + ": " + getValues(event.getOriginalRecord()) + " -> " + getValues(event.getNewRecord()));
    }

    private String getUser()
    {
        Actor actor = accessManager.getActor();
        if (actor == null)
        {
            return USER_UNKNOWN;
        }
        else if (actor.isAnonymous())
        {
            return USER_ANONYMOUS;
        }
        else
        {
            return actor.getUsername();
        }
    }

    private String getValues(Record record)
    {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        boolean first = true;
        for (String key: record.simpleKeySet())
        {
            if (first)
            {
                first = false;
            }
            else
            {
                builder.append(", ");
            }

            builder.append(key);
            builder.append(": ");

            if (key.toLowerCase().contains("password"))
            {
                builder.append("[scrubbed]");
            }
            else
            {
                Object value = record.get(key);
                if (value instanceof String)
                {
                    builder.append(value);
                }
                else
                {
                    boolean arrayFirst = true;
                    builder.append("[");
                    for (String item: (String[]) value)
                    {
                        if (arrayFirst)
                        {
                            arrayFirst = false;
                        }
                        else
                        {
                            builder.append(", ");
                        }

                        builder.append(item);
                    }
                    builder.append("]");
                }
            }
        }

        builder.append("}");
        return builder.toString();
    }

    public void handleEvent(Event event)
    {
        if (event instanceof RecordInsertedEvent)
        {
            handleRecordInserted((RecordInsertedEvent) event);
        }
        else if(event instanceof RecordDeletedEvent)
        {
            handleRecordDeleted((RecordDeletedEvent) event);
        }
        else if (event instanceof RecordUpdatedEvent)
        {
            handleRecordUpdated((RecordUpdatedEvent) event);
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{RecordEvent.class};
    }

    public void setEventManager(EventManager eventManager)
    {
        eventManager.register(this);
    }

    public void setAccessManager(AccessManager accessManager)
    {
        this.accessManager = accessManager;
    }
}
