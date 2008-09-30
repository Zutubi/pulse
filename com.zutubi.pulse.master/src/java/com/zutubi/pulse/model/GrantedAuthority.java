package com.zutubi.pulse.model;

/**
 * <class-comment/>
 */
public class GrantedAuthority implements org.acegisecurity.GrantedAuthority
{
    public static final String ANONYMOUS = "ROLE_ANONYMOUS";
    public static final String GUEST = "ROLE_GUEST";
    public static final String USER = "ROLE_USER";

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

    /**
     * For hibernate.
     * @param authority
     */
    private void setAuthority(String authority)
    {
        this.authority = authority;
    }

    public boolean equals(Object other)
    {
        if(other instanceof String)
        {
            // Acegi GrantedAuthorityEffectiveAclsResolver calls into us with
            // the role string
            return other.equals(authority);
        }
        else if(other instanceof GrantedAuthority)
        {
            return ((GrantedAuthority)other).authority.equals(authority);
        }
        else
        {
            return false;
        }
    }

    public int hashCode()
    {
        return authority.hashCode();
    }
}
