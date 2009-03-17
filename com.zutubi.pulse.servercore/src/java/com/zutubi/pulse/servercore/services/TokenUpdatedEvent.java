package com.zutubi.pulse.servercore.services;

import com.zutubi.events.Event;

/**
 * An event generated when the service token is updated.
 */
public class TokenUpdatedEvent extends Event
{
    private String token;
    public TokenUpdatedEvent(Object source, String token)
    {
        super(source);
        this.token = token;
    }

    public String getToken()
    {
        return token;
    }
}
