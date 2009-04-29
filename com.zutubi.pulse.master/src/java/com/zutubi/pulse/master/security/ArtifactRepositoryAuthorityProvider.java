package com.zutubi.pulse.master.security;

import com.zutubi.tove.security.AuthorityProvider;
import com.zutubi.tove.security.DefaultAccessManager;
import com.zutubi.tove.type.record.PathUtils;

import java.util.HashSet;
import java.util.Set;

public class ArtifactRepositoryAuthorityProvider implements AuthorityProvider<HttpInvocation>
{
    private AuthorityDefinitions definitions;

    public Set<String> getAllowedAuthorities(String action, HttpInvocation resource)
    {
        Set<String> allowedRoles = new HashSet<String>();

        // given the path and the requested action, what are the roles that have access?.
        for (Privilege privilege : definitions.getPrivileges())
        {
            if (PathUtils.prefixPatternMatchesPath(privilege.getPath(), resource.getPath()) && privilege.containsMethod(action))
            {
                allowedRoles.add(privilege.getRole());
            }
        }

        return allowedRoles;
    }

    public void setAccessManager(DefaultAccessManager accessManager)
    {
        accessManager.registerAuthorityProvider(HttpInvocation.class, this);
    }

    public void setDefinitions(AuthorityDefinitions definitions)
    {
        this.definitions = definitions;
    }
}