package com.zutubi.pulse.master.security;

import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectResponsibility;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.security.AuthorityProvider;
import com.zutubi.tove.security.DefaultAccessManager;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Maps from project ACL configurations to authorities allowed to perform
 * actions on projects.
 */
public class ProjectAuthorityProvider implements AuthorityProvider<Project>
{
    private ProjectConfigurationAuthorityProvider projectConfigurationAuthorityProvider;
    private AccessManager accessManager;

    public Set<String> getAllowedAuthorities(String action, Project resource)
    {
        if (action.equals(ProjectConfigurationActions.ACTION_TAKE_RESPONSIBILITY))
        {
            if (resource.getResponsibility() != null || accessManager.getActor().isAnonymous())
            {
                // Responsibility can't be taken by anonymous users or off
                // another user.
                return Collections.emptySet();
            }
            else
            {
                // Anyone who can view a project can be responsible for it.
                return projectConfigurationAuthorityProvider.getAllowedAuthorities(AccessManager.ACTION_VIEW, resource.getConfig());
            }
        }
        else if (action.equals(ProjectConfigurationActions.ACTION_CLEAR_RESPONSIBILITY))
        {
            Set<String> result = new HashSet<String>();
            ProjectResponsibility responsibility = resource.getResponsibility();
            if (responsibility != null)
            {
                // Only the responsible user can clear responsibility.
                result.add(responsibility.getUser().getConfig().getDefaultAuthority());
            }
            return result;
        }
        else
        {
            return projectConfigurationAuthorityProvider.getAllowedAuthorities(action, resource.getConfig());
        }
    }

    public void setAccessManager(DefaultAccessManager accessManager)
    {
        accessManager.registerAuthorityProvider(Project.class, this);
        this.accessManager = accessManager;
    }

    public void setProjectConfigurationAuthorityProvider(ProjectConfigurationAuthorityProvider projectConfigurationAuthorityProvider)
    {
        this.projectConfigurationAuthorityProvider = projectConfigurationAuthorityProvider;
    }
}
