package com.zutubi.pulse.security;

import com.zutubi.prototype.security.AuthorityProvider;
import com.zutubi.prototype.security.DefaultAccessManager;
import com.zutubi.pulse.model.BuildResult;

import java.util.HashSet;
import java.util.Set;

/**
 * Decides which authorities are allowed to perform actions on build results.
 * Personal builds are only viewable by the owner and admins, project builds
 * follow the ACLs for the project.
 */
public class BuildResultAuthorityProvider implements AuthorityProvider<BuildResult>
{
    private ProjectConfigurationAuthorityProvider projectConfigurationAuthorityProvider;

    public Set<String> getAllowedAuthorities(String action, BuildResult resource)
    {
        if(resource.isPersonal())
        {
            Set<String> result = new HashSet<String>();
            result.add(resource.getUser().getConfig().getDefaultAuthority());
            return result;
        }
        else
        {
            return projectConfigurationAuthorityProvider.getAllowedAuthorities(action, resource.getProject().getConfig());
        }
    }

    public void setAccessManager(DefaultAccessManager accessManager)
    {
        accessManager.registerProvider(BuildResult.class, this);
    }

    public void setProjectConfigurationAuthorityProvider(ProjectConfigurationAuthorityProvider projectConfigurationAuthorityProvider)
    {
        this.projectConfigurationAuthorityProvider = projectConfigurationAuthorityProvider;
    }
}
