package com.cinnamonbob.model;

import com.cinnamonbob.core.model.Entity;
import com.cinnamonbob.web.DefaultAction;
import org.acegisecurity.userdetails.UserDetails;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 *
 */
public class User extends Entity implements UserDetails
{
    /**
     * The login name is used to identify this user.
     */
    private String login;

    /**
     * The name of the user is the users full name.
     */
    private String name;

    /**
     * Indicates whether or not the user is enabled. Only enabled users can
     * log in to the system.
     */
    private boolean enabled;

    /**
     * The users password.
     */
    private String password;
    /**
     * The action to take the user to when they log in.  Usually their
     * dashboard, but can be a welcome page when they first sign up.
     */
    private String defaultAction = DefaultAction.DASHBOARD_ACTION;

    private List<ContactPoint> contactPoints;
    private List<GrantedAuthority> authorities;

    public User()
    {
    }

    public User(String login, String name)
    {
        this.login = login;
        this.name = name;
    }

    public User(String login, String name, String password, String... authorities)
    {
        this(login, name);
        this.password = password;
        this.authorities = new LinkedList<GrantedAuthority>();
        for (String authority : authorities)
        {
            this.authorities.add(new GrantedAuthority(this, authority));
        }
    }

    public String getLogin()
    {
        return login;
    }

    public void setLogin(String login)
    {
        this.login = login;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getDefaultAction()
    {
        return defaultAction;
    }

    public void setDefaultAction(String defaultAction)
    {
        this.defaultAction = defaultAction;
    }

    public void add(ContactPoint point)
    {
        getContactPoints().add(point);
        point.setUser(this);
    }

    public boolean remove(ContactPoint point)
    {
        if (contactPoints.remove(point))
        {
            point.setUser(null);
            return true;
        }
        return false;
    }

    private void setContactPoints(List<ContactPoint> l)
    {
        this.contactPoints = l;
    }

    public List<ContactPoint> getContactPoints()
    {
        if (contactPoints == null)
        {
            contactPoints = new LinkedList<ContactPoint>();
        }
        return contactPoints;
    }

    //TODO: may want to look into using hibernate to store a map of
    //TODO: name -> contactpoint.
    public ContactPoint getContactPoint(String name)
    {
        for (ContactPoint cp : contactPoints)
        {
            if (cp.getName().compareTo(name) == 0)
            {
                return cp;
            }
        }
        return null;
    }

    public ContactPoint getContactPoint(long contactPointId)
    {
        for (ContactPoint cp : contactPoints)
        {
            if (cp.getId() == contactPointId)
            {
                return cp;
            }
        }
        return null;
    }

    public List<Subscription> getSubscriptions()
    {
        List<Subscription> subscriptions = new LinkedList<Subscription>();
        for (ContactPoint cp : contactPoints)
        {
            subscriptions.addAll(cp.getSubscriptions());
        }
        return subscriptions;
    }

    public GrantedAuthority[] getAuthorities()
    {
        return getGrantedAuthorities().toArray(new GrantedAuthority[getGrantedAuthorities().size()]);
    }

    public List<GrantedAuthority> getGrantedAuthorities()
    {
        if (authorities == null)
        {
            authorities = new LinkedList<GrantedAuthority>();
        }
        return authorities;
    }

    private void setGrantedAuthorities(List<GrantedAuthority> authorities)
    {
        this.authorities = authorities;
    }

    public void add(String authority)
    {
        if (!hasAuthority(authority))
        {
            GrantedAuthority grantedAuthority = new GrantedAuthority(this, authority);
            getGrantedAuthorities().add(grantedAuthority);
        }
    }

    public void remove(String authority)
    {
        GrantedAuthority authorityToBeRemoved = null;
        for (GrantedAuthority grantedAuthority : getGrantedAuthorities())
        {
            if (grantedAuthority.getAuthority().equals(authority))
            {
                authorityToBeRemoved = grantedAuthority;
                break;
            }
        }
        // for this to actually delete the authority, we require cascade delete-orphan.
        // otherwise the reference from the authority to the user will keep it alive.
        getGrantedAuthorities().remove(authorityToBeRemoved);
    }

    public boolean hasAuthority(String authority)
    {
        for (GrantedAuthority grantedAuth : getGrantedAuthorities())
        {
            if (grantedAuth.getAuthority().equals(authority))
            {
                return true;
            }
        }
        return false;
    }

    public String getUsername()
    {
        return getLogin();
    }

    /**
     * @see org.acegisecurity.userdetails.UserDetails#isAccountNonExpired()
     */
    public boolean isAccountNonExpired()
    {
        return true;
    }

    /**
     * @see org.acegisecurity.userdetails.UserDetails#isAccountNonLocked()
     */
    public boolean isAccountNonLocked()
    {
        return true;
    }

    /**
     * @see org.acegisecurity.userdetails.UserDetails#isCredentialsNonExpired()
     */
    public boolean isCredentialsNonExpired()
    {
        return true;
    }
}
