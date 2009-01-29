package com.zutubi.pulse.servercore.jetty;

import static com.zutubi.util.CollectionUtils.map;
import static com.zutubi.util.CollectionUtils.contains;
import com.zutubi.util.Mapping;
import com.zutubi.util.Predicate;
import com.zutubi.tove.type.record.PathUtils;
import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;

import java.util.*;

/**
 * Implementation of the Privilege Evaluator interface that is configured using path, role, method tuples.
 * The path is a ant style path used to match the http request path, the role defines the roles that have
 * access to that path, and the methods define which methods the defined roles have access to.
 */
public class StaticRolePrivilegeEvaluator implements PrivilegeEvaluator
{
    private Map<String, List<Privilege>> roleBasedPrivileges = new HashMap<String, List<Privilege>>();

    public boolean isAllowed(final HttpInvocation invocation, Authentication auth)
    {
        if (invocation == null || auth == null)
        {
            return false;
        }
        
        // is a role present that contains the necessary privileges?
        return contains(getRoles(auth), new Predicate<String>()
        {
            public boolean satisfied(final String role)
            {
                return contains(getPrivileges(role), new Predicate<Privilege>()
                {
                    public boolean satisfied(Privilege privilege)
                    {
                        // is the path a prefix for the request path?
                        return PathUtils.prefixPatternMatchesPath(privilege.getPath(), invocation.getPath()) && privilege.containsMethod(invocation.getMethod());
                    }
                });
            }
        });
    }

    private List<String> getRoles(Authentication auth)
    {
        if (auth == null || auth.getAuthorities() == null)
        {
            return Collections.emptyList();
        }
        return map(auth.getAuthorities(), new Mapping<GrantedAuthority, String>()
        {
            public String map(GrantedAuthority grantedAuthority)
            {
                return grantedAuthority.getAuthority();
            }
        });
    }

    public void addPrivilege(String path, String role, String... methods)
    {
        getPrivileges(role).add(new Privilege(path, role, methods));
    }

    private List<Privilege> getPrivileges(String role)
    {
        if (!roleBasedPrivileges.containsKey(role))
        {
            roleBasedPrivileges.put(role, new LinkedList<Privilege>());
        }
        return roleBasedPrivileges.get(role);
    }

    protected List<Privilege> getPrivileges()
    {
        List<Privilege> result = new LinkedList<Privilege>();
        for (List<Privilege> privileges : roleBasedPrivileges.values())
        {
            result.addAll(privileges);
        }
        return result;
    }

}
