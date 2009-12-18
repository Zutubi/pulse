package com.zutubi.pulse.master.security;

import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.master.model.Comment;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.events.ConfigurationEventSystemStartedEvent;
import com.zutubi.tove.security.AuthorityProvider;
import com.zutubi.tove.security.DefaultAccessManager;
import com.zutubi.tove.type.record.PathUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Decides who can take actions on comments - only admins and the comment
 * owner.
 */
public class CommentAuthorityProvider implements AuthorityProvider<Comment>, EventListener
{
    private ConfigurationProvider configurationProvider;

    public Set<String> getAllowedAuthorities(String action, Comment resource)
    {
        UserConfiguration user = configurationProvider.get(PathUtils.getPath(MasterConfigurationRegistry.USERS_SCOPE, resource.getAuthor()), UserConfiguration.class);
        if (user == null)
        {
            return Collections.emptySet();
        }
        else
        {
            return new HashSet<String>(Arrays.asList(user.getDefaultAuthority()));
        }
    }

    public void setAccessManager(DefaultAccessManager accessManager)
    {
        accessManager.registerAuthorityProvider(Comment.class, this);
    }

    public void setEventManager(EventManager eventManager)
    {
        eventManager.register(this);
    }

    public void handleEvent(Event event)
    {
        this.configurationProvider = ((ConfigurationEventSystemStartedEvent)event).getConfigurationProvider();
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{ ConfigurationEventSystemStartedEvent.class };
    }
}
