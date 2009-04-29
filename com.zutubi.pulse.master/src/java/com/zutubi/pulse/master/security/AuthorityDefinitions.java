package com.zutubi.pulse.master.security;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;

public class AuthorityDefinitions
{
    private Map<String, List<Privilege>> pathBasedPrivileges = new HashMap<String, List<Privilege>>();

    public void addPrivilege(String path, String role, String... methods)
    {
        getPrivileges(path).add(new Privilege(path, role, methods));
    }

    protected List<Privilege> getPrivileges(String path)
    {
        if (!pathBasedPrivileges.containsKey(path))
        {
            pathBasedPrivileges.put(path, new LinkedList<Privilege>());
        }
        return pathBasedPrivileges.get(path);
    }

    protected List<Privilege> getPrivileges()
    {
        List<Privilege> result = new LinkedList<Privilege>();
        for (List<Privilege> privileges : pathBasedPrivileges.values())
        {
            result.addAll(privileges);
        }
        return result;
    }
}
