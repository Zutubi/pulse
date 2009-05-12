package com.zutubi.pulse.master.security;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.tove.security.AuthorityProvider;
import com.zutubi.tove.security.DefaultAccessManager;

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
        if (resource.isPersonal())
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
        accessManager.registerAuthorityProvider(BuildResult.class, this);
    }

    public void setProjectConfigurationAuthorityProvider(ProjectConfigurationAuthorityProvider projectConfigurationAuthorityProvider)
    {
        this.projectConfigurationAuthorityProvider = projectConfigurationAuthorityProvider;
    }
}
