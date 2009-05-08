package com.zutubi.pulse.master.security;

import com.zutubi.pulse.master.model.BuildResponsibility;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.security.AuthorityProvider;
import com.zutubi.tove.security.DefaultAccessManager;

import java.util.Collections;
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
    private AccessManager accessManager;

    public Set<String> getAllowedAuthorities(String action, BuildResult resource)
    {
        if (action.equals(BuildResult.ACTION_TAKE_RESPONSIBILITY))
        {
            if (resource.isPersonal() || resource.getResponsibility() != null || accessManager.getActor().isAnonymous())
            {
                // Responsibility can't be taken for personal builds
                // (nonsensical) or off another user.
                return Collections.emptySet();
            }
            else
            {
                // Anyone who can view a build can be responsible for it.
                return projectConfigurationAuthorityProvider.getAllowedAuthorities(AccessManager.ACTION_VIEW, resource.getProject().getConfig());
            }
        }
        else if (action.equals(BuildResult.ACTION_CLEAR_RESPONSIBILITY))
        {
            Set<String> result = new HashSet<String>();
            BuildResponsibility responsibility = resource.getResponsibility();
            if (responsibility != null)
            {
                // Only the responsible user can clear responsibility.
                result.add(responsibility.getUser().getConfig().getDefaultAuthority());
            }
            return result;
        }
        else
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
    }

    public void setAccessManager(DefaultAccessManager accessManager)
    {
        accessManager.registerAuthorityProvider(BuildResult.class, this);
        this.accessManager = accessManager;
    }

    public void setProjectConfigurationAuthorityProvider(ProjectConfigurationAuthorityProvider projectConfigurationAuthorityProvider)
    {
        this.projectConfigurationAuthorityProvider = projectConfigurationAuthorityProvider;
    }
}
