package com.cinnamonbob.model;

import com.cinnamonbob.core.model.Entity;

/**
 * <class-comment/>
 */
public class GrantedAuthority extends Entity implements org.acegisecurity.GrantedAuthority
{
    public static final GrantedAuthority USER = new GrantedAuthority("ROLE_USER");
    public static final GrantedAuthority ADMINISTRATOR = new GrantedAuthority("ROLE_ADMINISTRATOR");

    private User user;
    private String authority;


    public GrantedAuthority()
    {

    }

    public GrantedAuthority(String authority)
    {
        this.authority = authority;
    }

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
