package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.Entity;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * A group of users, which assigns to each of those users various authorities.
 * These authorities are then used in to determine access to objects etc.
 *
 * Every group has at least one authority, referred to as the "default"
 * authority, that is intrinsic to the group.  This authority is used when
 * assigning permissions for the group via ACLs.
 *
 * Additional authorities, such as ROLE_ADMINISTRATOR, may be added to the
 * group so that it inherits the permissions of that authority.
 */
public class Group extends Entity implements NamedEntity
{
    private static final String DEFAULT_AUTHORITY_PREFIX = "GROUP_";

    /**
     * Descriptive name for the group.  Also used as the basis for the
     * group's default authority.
     */
    private String name;
    /**
     * If true, this group is automatically granted admin permissions to
     * all projects.
     */
    private boolean adminAllProjects = false;
    /**
     * Additional authorities for the group.
     */
    private List<String> additionalAuthorities = new ArrayList<String>(2);
    /**
     * Users that are members of this group.
     */
    private Set<User> users = new HashSet<User>();

    public Group()
    {
    }

    public Group(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getAuthorityCount()
    {
        return additionalAuthorities.size() + 1;
    }

    public List<GrantedAuthority> getAuthorities()
    {
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>(additionalAuthorities.size() + 1);
        authorities.add(new GrantedAuthority(getDefaultAuthority()));
        for(String authority: additionalAuthorities)
        {
            authorities.add(new GrantedAuthority(authority));
        }
        return authorities;
    }

    public String getDefaultAuthority()
    {
        return DEFAULT_AUTHORITY_PREFIX + Long.toString(getId());
    }

    public boolean getAdminAllProjects()
    {
        return adminAllProjects;
    }

    public void setAdminAllProjects(boolean adminAllProjects)
    {
        this.adminAllProjects = adminAllProjects;
    }

    private List<String> getAdditionalAuthorities()
    {
        return additionalAuthorities;
    }

    private void setAdditionalAuthorities(List<String> additionalAuthorities)
    {
        this.additionalAuthorities = additionalAuthorities;
    }

    public void addAdditionalAuthority(String authority)
    {
        additionalAuthorities.add(authority);
    }

    public void removeAdditionalAuthority(String authority)
    {
        additionalAuthorities.remove(authority);
    }

    public boolean hasAuthority(String authority)
    {
        return authority.equals(getDefaultAuthority()) || additionalAuthorities.contains(authority);
    }

    public Set<User> getUsers()
    {
        return users;
    }

    private void setUsers(Set<User> users)
    {
        this.users = users;
    }

    public void addUser(User user)
    {
        users.add(user);
        user.addGroup(this);
    }

    public void removeUser(User user)
    {
        if(users.remove(user))
        {
            user.removeGroup(this);
        }
    }
}
