package com.zutubi.pulse.security;

import com.zutubi.pulse.master.model.Project;
import com.zutubi.tove.security.AuthorityProvider;
import com.zutubi.tove.security.DefaultAccessManager;

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
        accessManager.registerAuthorityProvider(Project.class, this);
    }

    public void setProjectConfigurationAuthorityProvider(ProjectConfigurationAuthorityProvider projectConfigurationAuthorityProvider)
    {
        this.projectConfigurationAuthorityProvider = projectConfigurationAuthorityProvider;
    }
}
