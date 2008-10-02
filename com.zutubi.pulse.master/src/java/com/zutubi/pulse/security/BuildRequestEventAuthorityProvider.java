package com.zutubi.pulse.security;

import com.zutubi.pulse.master.events.build.AbstractBuildRequestEvent;
import com.zutubi.pulse.model.User;
import com.zutubi.tove.security.AuthorityProvider;
import com.zutubi.tove.security.DefaultAccessManager;

import java.util.HashSet;
import java.util.Set;

/**
 * Controls access to cancellation of build requests.
 */
public class BuildRequestEventAuthorityProvider implements AuthorityProvider<AbstractBuildRequestEvent>
{
    private ProjectConfigurationAuthorityProvider projectConfigurationAuthorityProvider;

    public Set<String> getAllowedAuthorities(String action, AbstractBuildRequestEvent resource)
    {
        if(resource.isPersonal())
        {
            User user = (User) resource.getOwner();
            Set<String> result = new HashSet<String>();
            result.add(user.getConfig().getDefaultAuthority());
            return result;
        }
        else
        {
            return projectConfigurationAuthorityProvider.getAllowedAuthorities(action, resource.getProjectConfig());
        }
    }

    public void setAccessManager(DefaultAccessManager accessManager)
    {
        accessManager.registerAuthorityProvider(AbstractBuildRequestEvent.class, this);
    }

    public void setProjectConfigurationAuthorityProvider(ProjectConfigurationAuthorityProvider projectConfigurationAuthorityProvider)
    {
        this.projectConfigurationAuthorityProvider = projectConfigurationAuthorityProvider;
    }
}
