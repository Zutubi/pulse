package com.zutubi.pulse.security;

import com.zutubi.tove.security.Actor;
import com.zutubi.tove.security.ActorProvider;

/**
 * Adapts from Acegi logged-in users to the config systems ActorProvider
 * interface.
 */
public class AcegiActorProvider implements ActorProvider
{
    public Actor getActor()
    {
        return AcegiUtils.getLoggedInUser();
    }
}
