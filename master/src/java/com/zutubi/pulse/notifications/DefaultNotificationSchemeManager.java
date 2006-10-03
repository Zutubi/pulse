package com.zutubi.pulse.notifications;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * <class-comment/>
 */
public class DefaultNotificationSchemeManager implements NotificationSchemeManager
{
    private Map<String, Class<? extends NotificationHandler>> handlers = new HashMap<String, Class<? extends NotificationHandler>>();

    public DefaultNotificationSchemeManager()
    {
    }

    public void init()
    {
        // initialise the default handlers
        handlers.put("email", EmailNotificationHandler.class);
        handlers.put("jabber", JabberNotificationHandler.class);
    }

    public List<String> getNotificationSchemes()
    {
        return new LinkedList(handlers.keySet());
    }

    public Class<? extends NotificationHandler> getNotificationHandler(String scheme)
    {
        return handlers.get(scheme);
    }
}
