package com.zutubi.pulse.servercore.services;

import com.zutubi.events.EventListener;

/**
 * Notification interface that allows for changes to the
 * token managers stored token to be monitored.
 */
public interface TokenManagerListener extends EventListener
{
    /**
     * This callback is triggered when a change occures to the
     * managed token.
     *
     * @param token     the new token value.
     */
    void tokenUpdated(String token);
}
