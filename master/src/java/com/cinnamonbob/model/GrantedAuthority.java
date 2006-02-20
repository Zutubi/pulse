package com.cinnamonbob.model;

import com.cinnamonbob.core.model.Entity;

/**
 * <class-comment/>
 */
public class GrantedAuthority extends Entity
{
    private User user;
    private String authority;

    public String getAuthority()
    {
        return authority;
    }

    public void setAuthority(String authority)
    {
        this.authority = authority;
    }

    public User getUser()
    {
        return user;
    }

    public void setUser(User user)
    {
        this.user = user;
    }
}
