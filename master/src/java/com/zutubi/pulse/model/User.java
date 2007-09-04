package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.prototype.config.user.UserConfiguration;
import com.zutubi.pulse.prototype.config.user.UserPreferencesConfiguration;

import java.util.*;

/**
 * 
 *
 */
public class User extends Entity
{
    public static int REFRESH_DISABLED = 0;

    public static final String PROPERTY_DASHBOARD_BUILD_COUNT = "user.dashboardBuildCount";
    public static final String PROPERTY_DEFAULT_ACTION = "user.defaultAction";
    public static final String PROPERTY_LDAP_AUTHENTICATION = "user.ldapAuthentication";
    public static final String PROPERTY_SHOW_MY_CHANGES = "show.my.changes";
    public static final String PROPERTY_MY_CHANGES_COUNT = "my.changes.count";
    public static final String PROPERTY_SHOW_PROJECT_CHANGES = "show.project.changes";
    public static final String PROPERTY_PROJECT_CHANGES_COUNT = "project.changes.count";
    public static final String PROPERTY_REFRESH_INTERVAL = "user.refreshInterval";
    public static final String PROPERTY_TAIL_LINES = "tail.lines";
    public static final String PROPERTY_TAIL_REFRESH_INTERVAL = "tail.refresh.interval";
    public static final String PROPERTY_MY_BUILDS_COUNT = "my.builds.count";
    public static final String PROPERTY_SHOW_ALL_PROJECTS = "show.all.projects";
    public static final String PROPERTY_MY_BUILDS_COLUMNS = "my.builds.columns";
    public static final String PROPERTY_MY_PROJECTS_COLUMNS = "my.projects.columns";
    public static final String PROPERTY_ALL_PROJECTS_COLUMNS = "all.projects.columns";
    public static final String PROPERTY_PROJECT_SUMMARY_COLUMNS = "project.summary.columns";
    public static final String PROPERTY_PROJECT_RECENT_COLUMNS = "project.recent.columns";
    public static final String PROPERTY_PROJECT_HISTORY_COLUMNS = "project.history.columns";

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
     * Authorities granted directly to this user.  Authorities may also be
     * granted via the user's groups.
     */
    private List<String> authorities;

    /**
     * Groups this user is a member of.
     */
    private Set<Group> groups = new HashSet<Group>();

    private Map<String, String> properties;

    private long nextBuildNumber = 1;
    private UserConfiguration config;

    public User()
    {
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
        this.authorities = Arrays.asList(authorities);
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

    /**
     * Only returns authorities directly given to the user.  To get full
     * authority information, ask the UserManager for UserDetails.
     *
     * @return authorities granted directly to this user
     */
    List<String> getGrantedAuthorities()
    {
        if (authorities == null)
        {
            authorities = new ArrayList<String>();
        }
        return authorities;
    }

    private void setGrantedAuthorities(List<String> authorities)
    {
        this.authorities = authorities;
    }

    public void add(String authority)
    {
        if (!hasAuthority(authority))
        {
            getGrantedAuthorities().add(authority);
        }
    }

    public void remove(String authority)
    {
        // for this to actually delete the authority, we require cascade delete-orphan.
        // otherwise the reference from the authority to the user will keep it alive.
        getGrantedAuthorities().remove(authority);
    }

    public boolean hasAuthority(String authority)
    {
        return getGrantedAuthorities().contains(authority);
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

    public boolean getLdapAuthentication()
    {
        return getBooleanProperty(PROPERTY_LDAP_AUTHENTICATION, false);
    }

    public void setLdapAuthentication(boolean useLdap)
    {
        setBooleanProperty(PROPERTY_LDAP_AUTHENTICATION, useLdap);
    }

    public boolean equals(Object other)
    {
        return !(other == null || !(other instanceof User)) && login.equals(((User) other).login);
    }

    public int hashCode()
    {
        return login.hashCode();
    }

    private boolean getBooleanProperty(String property, boolean defaultValue)
    {
        if(hasProperty(property))
        {
            return Boolean.valueOf(getProperty(property));
        }

        return defaultValue;
    }

    private void setBooleanProperty(String property, boolean value)
    {
        setProperty(property, Boolean.toString(value));
    }

    private int getIntProperty(String property, int defaultValue)
    {
        if(hasProperty(property))
        {
            return Integer.parseInt(getProperty(property));
        }

        return defaultValue;
    }

    private void setIntProperty(String property, int value)
    {
        setProperty(property, Integer.toString(value));
    }

    private String getStringProperty(String property, String defaultValue)
    {
        if(hasProperty(property))
        {
            return getProperty(property);
        }

        return defaultValue;
    }

    public Set<Group> getGroups()
    {
        return groups;
    }

    private void setGroups(Set<Group> groups)
    {
        this.groups = groups;
    }

    /**
     * Add a group that this user has become a member of.  Package local:
     * edit this association from the group side!
     *
     * @param group the group to add
     */
    void addGroup(Group group)
    {
        groups.add(group);
    }

    /**
     * Remove a group that this user is no longer a member of.  Package local:
     * edit this association from the group side!
     *
     * @param group the group to remove
     */
    void removeGroup(Group group)
    {
        groups.remove(group);
    }

    public long getNextBuildNumber()
    {
        return nextBuildNumber;
    }

    public void setNextBuildNumber(long nextBuildNumber)
    {
        this.nextBuildNumber = nextBuildNumber;
    }

    public UserConfiguration getConfig()
    {
        return config;
    }

    public void setConfig(UserConfiguration config)
    {
        this.config = config;
    }

    public UserPreferencesConfiguration getPreferences()
    {
        if (config == null)
        {
            return null;
        }
        else
        {
            return config.getPreferences();
        }
    }
}
