package com.zutubi.pulse.model;

import com.zutubi.pulse.spring.SpringComponentContext;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.system.ConfigurationEventSystemStartedEvent;
import com.zutubi.pulse.events.system.ConfigurationSystemStartedEvent;
import com.zutubi.pulse.license.LicenseManager;
import com.zutubi.pulse.license.authorisation.AddUserAuthorisation;
import com.zutubi.pulse.model.persistence.UserDao;
import com.zutubi.pulse.security.ldap.LdapManager;
import com.zutubi.pulse.tove.config.group.AbstractGroupConfiguration;
import com.zutubi.pulse.tove.config.group.BuiltinGroupConfiguration;
import com.zutubi.pulse.tove.config.group.GroupConfiguration;
import com.zutubi.pulse.tove.config.user.DashboardConfiguration;
import com.zutubi.pulse.tove.config.user.UserConfiguration;
import com.zutubi.tove.config.ConfigurationEventListener;
import com.zutubi.tove.config.ConfigurationInjector;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.ConfigurationRegistry;
import com.zutubi.tove.config.ConfigurationStateManager;
import com.zutubi.tove.config.ExternalStateManager;
import com.zutubi.tove.config.TypeAdapter;
import com.zutubi.tove.config.TypeListener;
import com.zutubi.tove.config.events.ConfigurationEvent;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.TextUtils;
import org.acegisecurity.providers.encoding.PasswordEncoder;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.acegisecurity.userdetails.UserDetails;
import org.springframework.dao.DataAccessException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 *
 */
public class DefaultUserManager implements UserManager, ExternalStateManager<UserConfiguration>, ConfigurationInjector.ConfigurationSetter<User>, com.zutubi.pulse.events.EventListener
{
    private UserDao userDao;
    private PasswordEncoder passwordEncoder;

    private LicenseManager licenseManager;
    /**
     * Do not access directly, always use getBuildManager().  This dependency
     * is initialised on demand (not available when this manager is created).
     */
    private BuildManager buildManager;
    /**
     * Do not access directly, always use getLdapManager().  This dependency
     * is initialised on demand (not available when this manager is created).
     */
    private LdapManager ldapManager;
    private ConfigurationProvider configurationProvider;
    private Map<UserConfiguration, List<GroupConfiguration>> groupsByUser;
    private Map<Long, UserConfiguration> userConfigsById = new HashMap<Long, UserConfiguration>();
    private BuiltinGroupConfiguration allUsersGroup;

    private void registerConfigListeners(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;

        configurationProvider.registerEventListener(new ConfigurationEventListener()
        {
            public void handleConfigurationEvent(ConfigurationEvent event)
            {
                if(event.isPost())
                {
                    initGroupsByUser();
                }
            }
        }, true, true, ConfigurationRegistry.GROUPS_SCOPE);

        TypeListener<UserConfiguration> userListener = new TypeAdapter<UserConfiguration>(UserConfiguration.class)
        {
            public void postInsert(UserConfiguration instance)
            {
                userConfigsById.put(instance.getUserId(), instance);
            }

            public void postDelete(UserConfiguration instance)
            {
                // Deleting a user removes them from groups
                initGroupsByUser();

                userConfigsById.remove(instance.getUserId());
            }

            public void postSave(UserConfiguration instance, boolean nested)
            {
                userConfigsById.remove(instance.getUserId());
                userConfigsById.put(instance.getUserId(), instance);
            }
        };
        userListener.register(configurationProvider, true);
    }

    public void init()
    {
        // register the canAddUser license authorisation
        AddUserAuthorisation addUserAuthorisation = new AddUserAuthorisation();
        addUserAuthorisation.setUserManager(this);
        licenseManager.addAuthorisation(addUserAuthorisation);

        initGroupsByUser();
        initUsersById();
    }

    public long createState(UserConfiguration instance)
    {
        User user = new User();
        userDao.save(user);

        return user.getId();
    }

    public void rollbackState(long id)
    {
        User user = userDao.findById(id);
        if (user != null)
        {
            userDao.delete(user);
        }
    }

    public Object getState(long id)
    {
        return userDao.findById(id);
    }

    private void initGroupsByUser()
    {
        groupsByUser = new HashMap<UserConfiguration, List<GroupConfiguration>>();
        for(AbstractGroupConfiguration abstractGroup: configurationProvider.getAll(AbstractGroupConfiguration.class))
        {
            if(abstractGroup instanceof GroupConfiguration)
            {
                GroupConfiguration group = (GroupConfiguration) abstractGroup;
                for(UserConfiguration member: group.getMembers())
                {
                    List<GroupConfiguration> userGroups = groupsByUser.get(member);
                    if(userGroups == null)
                    {
                        userGroups = new LinkedList<GroupConfiguration>();
                        groupsByUser.put(member, userGroups);
                    }

                    userGroups.add(group);
                }
            }
        }

        allUsersGroup = getBuiltinGroup(ALL_USERS_GROUP_NAME);
    }

    private void initUsersById()
    {
        for(UserConfiguration user: configurationProvider.getAll(UserConfiguration.class))
        {
            userConfigsById.put(user.getUserId(), user);
        }
    }

    private BuiltinGroupConfiguration getBuiltinGroup(String name)
    {
        return configurationProvider.get(PathUtils.getPath(ConfigurationRegistry.GROUPS_SCOPE, name), BuiltinGroupConfiguration.class);
    }

    /**
     * @deprecated
     */
    public void save(User user)
    {
        userDao.save(user);
    }

    public UserConfiguration getUserConfig(String login)
    {
        return configurationProvider.get(PathUtils.getPath(ConfigurationRegistry.USERS_SCOPE, login), UserConfiguration.class);
    }

    public User getUser(String login)
    {
        UserConfiguration config = getUserConfig(login);
        if(config == null)
        {
            return null;
        }
        else
        {
            return userDao.findById(config.getUserId());
        }
    }

    public User getUser(long id)
    {
        return userDao.findById(id);
    }

    public List<User> getAllUsers()
    {
        return userDao.findAll();
    }

    public void delete(User user)
    {
        getBuildManager().deleteAllBuilds(user);
        userDao.delete(user);
        licenseManager.refreshAuthorisations();
    }

    public long getNextBuildNumber(User user)
    {
        user = getUser(user.getId());
        long number = user.getNextBuildNumber();
        user.setNextBuildNumber(number + 1);
        userDao.save(user);
        return number;
    }

    public GroupConfiguration getGroupConfig(String name)
    {
        return configurationProvider.get(PathUtils.getPath(ConfigurationRegistry.GROUPS_SCOPE, name), GroupConfiguration.class);
    }

    public Set<Project> getUserProjects(User user, final ProjectManager projectManager)
    {
        Set<Project> projects = new HashSet<Project>();
        DashboardConfiguration dashboardConfig = user.getConfig().getPreferences().getDashboard();
        if(dashboardConfig.isShowAllProjects())
        {
            projects.addAll(projectManager.getProjects(true));
        }
        else
        {
            projects.addAll(projectManager.mapConfigsToProjects(dashboardConfig.getShownProjects()));
            List<String> groupNames = user.getPreferences().getDashboard().getShownGroups();
            for(String groupName: groupNames)
            {
                ProjectGroup group = projectManager.getProjectGroup(groupName);
                projects.addAll(group.getProjects());
            }
        }

        return projects;
    }

    public AcegiUser getPrinciple(User user)
    {
        AcegiUser principle = new AcegiUser(user, groupsByUser.get(user.getConfig()));
        principle.addGroup(allUsersGroup);
        getLdapManager().addLdapRoles(principle);
        return principle;
    }

    public int getUserCount()
    {
        return userDao.count();
    }

    /**
     * Only for use by Acegi.  Calling this method directly is dangerous, as
     * the returned details are not fully initialised!  Use #getUserDetails
     * instead.
     *
     * @param username login of the user to retrieve
     * @return the user details for the given login
     * @throws UsernameNotFoundException
     * @throws DataAccessException
     */
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException
    {
        if (!TextUtils.stringSet(username))
        {
            throw new UsernameNotFoundException("User not set");
        }
        
        User user = getUser(username);
        if (user == null)
        {
            throw new UsernameNotFoundException("Unknown user");
        }

        AcegiUser principle = new AcegiUser(user, groupsByUser.get(user.getConfig()));
        principle.addGroup(allUsersGroup);
        return principle;
    }

    public void setPassword(UserConfiguration user, String rawPassword)
    {
        String encodedPassword = passwordEncoder.encodePassword(rawPassword, null);
        user.setPassword(encodedPassword);
        configurationProvider.save(user);
    }

    public void handleEvent(Event event)
    {
        if(event instanceof ConfigurationEventSystemStartedEvent)
        {
            registerConfigListeners(((ConfigurationEventSystemStartedEvent)event).getConfigurationProvider());
        }
        else
        {
            init();
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{ConfigurationEventSystemStartedEvent.class, ConfigurationSystemStartedEvent.class };
    }

    public void setPasswordEncoder(PasswordEncoder passwordEncoder)
    {
        this.passwordEncoder = passwordEncoder;
    }

    public void setUserDao(UserDao userDao)
    {
        this.userDao = userDao;
    }

    public void setLicenseManager(LicenseManager licenseManager)
    {
        this.licenseManager = licenseManager;
    }

    public BuildManager getBuildManager()
    {
        if(buildManager == null)
        {
            buildManager = (BuildManager) SpringComponentContext.getBean("buildManager");
        }
        return buildManager;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public LdapManager getLdapManager()
    {
        if(ldapManager == null)
        {
            ldapManager = (LdapManager) SpringComponentContext.getBean("ldapManager");
        }
        return ldapManager;
    }

    public void setLdapManager(LdapManager ldapManager)
    {
        this.ldapManager = ldapManager;
    }

    public void setConfiguration(User state)
    {
        state.setConfig(userConfigsById.get(state.getId()));
    }

    public void setConfigurationStateManager(ConfigurationStateManager configurationStateManager)
    {
        configurationStateManager.register(UserConfiguration.class, this);
    }

    public void setConfigurationInjector(ConfigurationInjector configurationInjector)
    {
        configurationInjector.registerSetter(User.class, this);
    }

    public void setEventManager(EventManager eventManager)
    {
        eventManager.register(this);
    }
}
