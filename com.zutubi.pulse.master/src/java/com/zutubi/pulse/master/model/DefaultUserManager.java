package com.zutubi.pulse.master.model;

import com.google.common.base.Objects;
import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.master.model.persistence.UserDao;
import com.zutubi.pulse.master.security.Principle;
import com.zutubi.pulse.master.security.SecurityUtils;
import com.zutubi.pulse.master.security.ldap.LdapManager;
import com.zutubi.pulse.master.tove.config.ConfigurationInjector;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.group.BuiltinGroupConfiguration;
import com.zutubi.pulse.master.tove.config.group.GroupConfiguration;
import com.zutubi.pulse.master.tove.config.group.UserGroupConfiguration;
import com.zutubi.pulse.master.tove.config.user.DashboardConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.pulse.servercore.events.system.SystemStartedListener;
import com.zutubi.tove.config.*;
import com.zutubi.tove.config.events.ConfigurationEvent;
import com.zutubi.tove.events.ConfigurationEventSystemStartedEvent;
import com.zutubi.tove.events.ConfigurationSystemStartedEvent;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

/**
 *
 *
 */
public class DefaultUserManager implements UserManager, ExternalStateManager<UserConfiguration>, ConfigurationInjector.ConfigurationSetter<User>, EventListener
{
    private UserDao userDao;
    private PasswordEncoder passwordEncoder;
    private org.springframework.security.authentication.encoding.PasswordEncoder legacyPasswordEncoder;

    private BuildManager buildManager;
    private LdapManager ldapManager;
    private ProjectManager projectManager;

    private ConfigurationProvider configurationProvider;
    private Map<UserConfiguration, List<UserGroupConfiguration>> groupsByUser;
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
        }, true, true, MasterConfigurationRegistry.GROUPS_SCOPE);

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
        groupsByUser = new HashMap<UserConfiguration, List<UserGroupConfiguration>>();
        for(GroupConfiguration group : configurationProvider.getAll(GroupConfiguration.class))
        {
            if(group instanceof UserGroupConfiguration)
            {
                UserGroupConfiguration userGroup = (UserGroupConfiguration) group;
                for(UserConfiguration member: userGroup.getMembers())
                {
                    List<UserGroupConfiguration> userGroups = groupsByUser.get(member);
                    if(userGroups == null)
                    {
                        userGroups = new LinkedList<UserGroupConfiguration>();
                        groupsByUser.put(member, userGroups);
                    }

                    userGroups.add(userGroup);
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
        return configurationProvider.get(PathUtils.getPath(MasterConfigurationRegistry.GROUPS_SCOPE, name), BuiltinGroupConfiguration.class);
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
        return configurationProvider.get(PathUtils.getPath(MasterConfigurationRegistry.USERS_SCOPE, login), UserConfiguration.class);
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
        buildManager.deleteAllBuilds(user);
        userDao.delete(user);
    }

    public void clearAllResponsibilities(User user)
    {
        projectManager.clearResponsibilities(user);
    }

    public int getConcurrentPersonalBuilds(User user)
    {
        int max = allUsersGroup.getConcurrentPersonalBuilds();
        List<UserGroupConfiguration> groups = groupsByUser.get(user.getConfig());
        if (groups != null)
        {
            for (UserGroupConfiguration group : groups)
            {
                if (group.getConcurrentPersonalBuilds() > max)
                {
                    max = group.getConcurrentPersonalBuilds();
                }
            }
        }

        return max;
    }

    public long updateAndGetNextBuildNumber(User user)
    {
        user = getUser(user.getId());
        long number = user.getNextBuildNumber();
        user.setNextBuildNumber(number + 1);
        userDao.save(user);
        return number;
    }

    public UserConfiguration insert(UserConfiguration user)
    {
        // insert the new user configuration instance.  Note that the state instance will be
        // created separately via a call to {@link UserManager#createState}
        String insertedPath = configurationProvider.insert(MasterConfigurationRegistry.USERS_SCOPE, user);
        return configurationProvider.get(insertedPath, UserConfiguration.class);
    }

    public UserGroupConfiguration getGroupConfig(String name)
    {
        return configurationProvider.get(PathUtils.getPath(MasterConfigurationRegistry.GROUPS_SCOPE, name), UserGroupConfiguration.class);
    }

    public Collection<UserConfiguration> getGroupMembers(GroupConfiguration group)
    {
        if (group instanceof UserGroupConfiguration)
        {
            return Collections.unmodifiableList(((UserGroupConfiguration) group).getMembers());
        }
        else if (group.getName().equals(ALL_USERS_GROUP_NAME))
        {
            return configurationProvider.getAll(PathUtils.getPath(MasterConfigurationRegistry.USERS_SCOPE, PathUtils.WILDCARD_ANY_ELEMENT), UserConfiguration.class);
        }
        else
        {
            return Collections.emptyList();
        }
    }

    public Set<Project> getUserProjects(User user, final ProjectManager projectManager)
    {
        Set<Project> projects = new HashSet<Project>();
        final DashboardConfiguration dashboardConfig = user.getConfig().getPreferences().getDashboard();
        if(dashboardConfig.isShowAllProjects())
        {
            projects.addAll(projectManager.getProjects(true));
        }
        else
        {
            projects.addAll(projectManager.mapConfigsToProjects(dashboardConfig.getShownProjects()));
        }

        // When groups are filtered and ungrouped projects hidden, not all the
        // projects selected above are included.  Collect only those that are
        // both selected above *and* are members of a selected group.
        if (!dashboardConfig.isShowAllGroups() && !dashboardConfig.isShowUngrouped())
        {
            Set<Project> groupedProjects = new HashSet<Project>();
            for (String groupName: dashboardConfig.getShownGroups())
            {
                ProjectGroup group = projectManager.getProjectGroup(groupName);
                if (group != null)
                {
                    for (Project p: group.getProjects())
                    {
                        if (projects.contains(p))
                        {
                            groupedProjects.add(p);
                        }
                    }
                }
            }

            projects = groupedProjects; 
        }

        return projects;
    }

    public Principle getPrinciple(UserConfiguration userConfig)
    {
        User user = getUser(userConfig.getUserId());
        return getPrinciple(user);
    }

    public Principle getPrinciple(User user)
    {
        Principle principle = new Principle(user, groupsByUser.get(user.getConfig()));
        principle.addGroup(allUsersGroup);
        ldapManager.addLdapRoles(principle);
        return principle;
    }

    public int getUserCount()
    {
        return (int) userDao.count();
    }

    /**
     * Only for use by Acegi.  Calling this method directly is dangerous, as
     * the returned details are not fully initialised!
     *
     * @param username login of the user to retrieve
     * @return the user details for the given login
     * @throws UsernameNotFoundException
     * @throws DataAccessException
     */
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException
    {
        if (!StringUtils.stringSet(username))
        {
            throw new UsernameNotFoundException("User not set");
        }
        
        User user = getUser(username);
        if (user == null)
        {
            throw new UsernameNotFoundException("Unknown user");
        }

        return getPrinciple(user);
    }

    public boolean checkPassword(final UserConfiguration user, final String password)
    {
        if (passwordEncoder.matches(password, user.getPassword()))
        {
            return true;
        }
        else
        {
            // Previously user passwords were stored with a weaker hashing scheme.  We can't
            // upgrade them in one go as we don't know the original passwords, so instead we do
            // them one-by-one as the user logs in.  To do so, check passwords that fail with the
            // new encoder to see if they work with the old one.  If so case we re-encode,
            // upgrading that password to the new scheme.  Otherwise it was a true login failure.
            String encodedPassword = legacyPasswordEncoder.encodePassword(password, null);
            if (Objects.equal(user.getPassword(), encodedPassword))
            {
                SecurityUtils.runAsSystem(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        setPassword(user, password);
                    }
                });
                return true;
            }
        }

        return false;
    }

    public void setPassword(UserConfiguration user, String rawPassword)
    {
        String encodedPassword = passwordEncoder.encode(rawPassword);
        user = configurationProvider.deepClone(user);
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

    public void setLegacyPasswordEncoder(org.springframework.security.authentication.encoding.PasswordEncoder legacyPasswordEncoder)
    {
        this.legacyPasswordEncoder = legacyPasswordEncoder;
    }

    public void setUserDao(UserDao userDao)
    {
        this.userDao = userDao;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
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
        eventManager.register(new SystemStartedListener()
        {
            public void systemStarted()
            {
                SpringComponentContext.autowire(DefaultUserManager.this);
            }
        });
        eventManager.register(this);
    }
}
