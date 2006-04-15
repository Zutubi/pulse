/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.Entity;

/**
 * <class-comment/>
 */
public class GrantedAuthority extends Entity implements org.acegisecurity.GrantedAuthority
{
    public static final String USER = "ROLE_USER";
    public static final String ADMINISTRATOR = "ROLE_ADMINISTRATOR";

    private User user;
    private String authority;

    public GrantedAuthority()
    {

    }

    public GrantedAuthority(User user, String authority)
    {
        this.authority = authority;
        this.user = user;
    }

    public String getAuthority()
    {
        return authority;
    }

    /**
     * For hibernate.
     * @param authority
     */
    private void setAuthority(String authority)
    {
        this.authority = authority;
    }

    public User getUser()
    {
        return user;
    }

    /**
     * For hibernate.
     *
     * @param user
     */
    private void setUser(User user)
    {
        this.user = user;
    }

    public boolean equals(Object other)
    {
        if(other instanceof String)
        {
            // Acegi GrantedAuthorityEffectiveAclsResolver calls into us with
            // the role string
            return other.equals(authority);
        }

        return super.equals(other);
    }
}
