package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.web.DefaultAction;
import org.acegisecurity.userdetails.UserDetails;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 
 *
 */
public class User extends Entity implements UserDetails
{
    public static int REFRESH_DISABLED = 0;

    private static final String PROPERTY_LDAP_AUTHENTICATION = "user.ldapAuthentication";

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
     * Contact points configured by the user for notifications.
     */
    private List<ContactPoint> contactPoints;

    /**
     * List of projects the user wants to display on their dashboard.
     */
    private List<Project> projects = new LinkedList<Project>();

    private List<GrantedAuthority> authorities;

    private Map<String, String> properties;

    /**
     * A list of login aliases for the user: logins in other tools that pulse
     * may be interacting with (e.g. SCMs).
     */
    private List<String> aliases;


    public User()
    {
        aliases = new LinkedList<String>();
    }

    public User(String login, String name)
    {
        this();
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

    /**
     * NOTE: Do not use this  method to update the users password. Please use the
     * userManager instead.
     *
     * @param encodedPassword
     *
     * @see com.zutubi.pulse.model.UserManager#setPassword(User, String)
     */
    public void setPassword(String encodedPassword)
    {
        this.password = encodedPassword;
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

    public Subscription getSubscription(long id)
    {
        for(Subscription s: getSubscriptions())
        {
            if(s.getId() == id)
            {
                return s;
            }
        }

        return null;
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

    public List<String> getAliases()
    {
        return aliases;
    }

    private void setAliases(List<String> aliases)
    {
        this.aliases = aliases;
    }

    public void addAlias(String alias)
    {
        aliases.add(alias);
    }

    public boolean removeAlias(String alias)
    {
        return aliases.remove(alias);
    }

    public void removeAlias(int index)
    {
        if (index <= aliases.size())
        {
            aliases.remove(index);
        }
    }

    public boolean hasAlias(String alias)
    {
        return aliases.contains(alias);
    }

    public List<Project> getProjects()
    {
        return projects;
    }

    public void setProjects(List<Project> projects)
    {
        this.projects = projects;
    }

    public void addProject(Project p)
    {
        projects.add(p);
    }

    public void clearProjects()
    {
        projects = new LinkedList<Project>();
    }

    public void setProperties(Map<String, String> props)
    {
        this.properties = props;
    }

    public Map<String, String> getProperties()
    {
        if (properties == null)
        {
            properties = new HashMap<String, String>();
        }
        return properties;
    }

    public void setProperty(String key, String value)
    {
        getProperties().put(key, value);
    }

    public String getProperty(String key)
    {
        return getProperties().get(key);
    }

    public boolean hasProperty(String key)
    {
        return getProperties().containsKey(key);
    }

    // ---( for backward compatibility and ease of migration. )---
    /**
     * The action to take the user to when they log in.  Usually their
     * dashboard, but can be a welcome page when they first sign up.
     */
    public String getDefaultAction()
    {
        if (hasProperty("user.defaultAction"))
        {
            return getProperty("user.defaultAction");
        }
        return DefaultAction.DASHBOARD_ACTION;
    }

    public void setDefaultAction(String defaultAction)
    {
        setProperty("user.defaultAction", defaultAction);
    }

    /**
     * If true, show all projects on the user's dashboard.
     */
    public boolean getShowAllProjects()
    {
        if (hasProperty("user.showAllProjects"))
        {
            return Boolean.valueOf(getProperty("user.showAllProjects"));
        }
        return true;
    }

    public void setShowAllProjects(boolean showAllProjects)
    {
        setProperty("user.showAllProjects", Boolean.toString(showAllProjects));
    }

    /**
     * Number of seconds between refreshes of "live" content, or 0 if the
     * user disables refreshing.
     */
    public int getRefreshInterval()
    {
        if (hasProperty("user.refreshInterval"))
        {
            return Integer.valueOf(getProperty("user.refreshInterval"));
        }
        return 60;
    }

    public void setRefreshInterval(int refreshInterval)
    {
        setProperty("user.refreshInterval", Integer.toString(refreshInterval));
    }

    public boolean getLdapAuthentication()
    {
        if (hasProperty(PROPERTY_LDAP_AUTHENTICATION))
        {
            return Boolean.valueOf(getProperty(PROPERTY_LDAP_AUTHENTICATION));
        }

        return false;
    }

    public void setLdapAuthentication(boolean useLdap)
    {
        setProperty(PROPERTY_LDAP_AUTHENTICATION, Boolean.toString(useLdap));
    }

    public boolean equals(Object other)
    {
        if(other == null || !(other instanceof User))
        {
            return false;
        }

        return login.equals(((User)other).login);
    }

    public int hashCode()
    {
        return login.hashCode();
    }
}
