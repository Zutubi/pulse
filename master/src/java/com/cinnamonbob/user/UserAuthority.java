package com.cinnamonbob.user;

import com.cinnamonbob.core.model.Entity;
import org.acegisecurity.GrantedAuthority;

/**
 * <class-comment/>
 */
public class UserAuthority extends Entity implements GrantedAuthority
{
    private User user;
    private Roles authority;

    public static UserAuthority createAdministrationAuth(User user)
    {
        UserAuthority auth = new UserAuthority();
        auth.setAuthority(Roles.ROLE_ADMINISTRATION.toString());
        auth.setUser(user);
        return auth;
    }

    public static UserAuthority createAuthenticatedAuth(User user)
    {
        UserAuthority auth = new UserAuthority();
        auth.setAuthority(Roles.ROLE_AUTHENTICATED.toString());
        auth.setUser(user);
        return auth;
    }

    public String getAuthority()
    {
        return authority.toString();
    }

    public void setAuthority(String authority)
    {
        this.authority = Roles.valueOf(authority);
    }

    public User getUser()
    {
        return user;
    }

    public void setUser(User user)
    {
        this.user = user;
        this.user.add(this);
    }
}
