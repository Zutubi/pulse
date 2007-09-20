package com.zutubi.pulse.security;

import com.zutubi.prototype.security.AuthorityProvider;
import com.zutubi.prototype.security.DefaultAccessManager;
import com.zutubi.pulse.model.Project;

import java.util.Set;

/**
 * Maps from project ACL configurations to authorities allowed to perform
 * actions on projects.
 */
public class ProjectAuthorityProvider implements AuthorityProvider<Project>
{
    private ProjectConfigurationAuthorityProvider projectConfigurationAuthorityProvider;

    public Set<String> getAllowedAuthorities(String action, Project resource)
    {
        return projectConfigurationAuthorityProvider.getAllowedAuthorities(action, resource.getConfig());
    }

    public void setAccessManager(DefaultAccessManager accessManager)
    {
        accessManager.registerProvider(Project.class, this);
    }

    public void setProjectConfigurationAuthorityProvider(ProjectConfigurationAuthorityProvider projectConfigurationAuthorityProvider)
    {
        this.projectConfigurationAuthorityProvider = projectConfigurationAuthorityProvider;
    }
}
