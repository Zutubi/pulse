package com.zutubi.pulse.master.security;

import com.zutubi.tove.security.Actor;
import com.zutubi.tove.security.ActorProvider;

/**
 * Adapts from Spring Security logged-in users to the config systems ActorProvider
 * interface.
 */
public class SpringSecurityActorProvider implements ActorProvider
{
    public Actor getActor()
    {
        return SecurityUtils.getLoggedInUser();
    }
}
