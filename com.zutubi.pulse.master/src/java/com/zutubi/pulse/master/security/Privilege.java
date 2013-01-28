package com.zutubi.pulse.master.security;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A simple holder for the path, role, methods tuple.  The represented data is defined as follows:
 * <ul>
 * <li>Path</li> is the http request path for which this privilege is being applied.
 * <li>Role</li> is the role required to qualify for this privilege.
 * <li>Methods</li> are the methods that are 'allowed' by this privilege. 
 * </ul>
 */
public class Privilege
{
    private String path;
    private String role;
    private Set<String> methods;

    protected Privilege(String path, String role, String... methods)
    {
        this.path = path;
        this.role = role;
        this.methods = new HashSet<String>();
        this.methods.addAll(Arrays.asList(methods));
    }

    public String getPath()
    {
        return path;
    }

    public String getRole()
    {
        return role;
    }

    public Set<String> getMethods()
    {
        return methods;
    }

    public boolean containsMethod(final String method)
    {
        return methods.contains(method);
    }
}
