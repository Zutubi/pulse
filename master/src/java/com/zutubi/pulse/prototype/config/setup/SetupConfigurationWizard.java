package com.zutubi.pulse.prototype.config.setup;

import com.zutubi.prototype.config.ConfigurationReferenceManager;
import com.zutubi.prototype.config.ConfigurationRegistry;
import com.zutubi.prototype.type.*;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.wizard.WizardTransition;
import com.zutubi.prototype.wizard.webwork.AbstractTypeWizard;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.bootstrap.SetupManager;
import com.zutubi.pulse.bootstrap.SystemConfigurationSupport;
import com.zutubi.pulse.model.AcegiUser;
import com.zutubi.pulse.model.GrantedAuthority;
import com.zutubi.pulse.model.UserManager;
import com.zutubi.pulse.prototype.config.admin.EmailConfiguration;
import com.zutubi.pulse.prototype.config.admin.GeneralAdminConfiguration;
import com.zutubi.pulse.prototype.config.admin.GlobalConfiguration;
import com.zutubi.pulse.prototype.config.group.BuiltinGroupConfiguration;
import com.zutubi.pulse.prototype.config.group.GroupConfiguration;
import com.zutubi.pulse.prototype.config.group.ServerPermission;
import com.zutubi.pulse.prototype.config.user.UserConfiguration;
import com.zutubi.pulse.security.AcegiUtils;
import com.zutubi.util.logging.Logger;
import org.acegisecurity.providers.encoding.PasswordEncoder;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadFactory;

/**
 */
public class SetupConfigurationWizard extends AbstractTypeWizard
{
    private static final Logger LOG = Logger.getLogger(SetupConfigurationWizard.class);

    private CompositeType adminConfigType;
    private CompositeType serverConfigType;

    private UserManager userManager;
    private MasterConfigurationManager configurationManager;
    private SetupManager setupManager;
    private ConfigurationReferenceManager configurationReferenceManager;
    private ThreadFactory threadFactory;
    private PasswordEncoder passwordEncoder;

    public void initialise()
    {
        adminConfigType = typeRegistry.getType(AdminUserConfiguration.class);
        serverConfigType = typeRegistry.getType(ServerSettingsConfiguration.class);

        List<AbstractChainableState> states = addWizardStates(null, ConfigurationRegistry.SETUP_SCOPE, adminConfigType, null);
        states = addWizardStates(states, ConfigurationRegistry.SETUP_SCOPE, serverConfigType, null);

        // a bit of custom initialisation
        SystemConfigurationSupport systemConfig = (SystemConfigurationSupport) configurationManager.getSystemConfig();

        MutableRecord record = states.get(0).getDataRecord();
        record.put("baseUrl", systemConfig.getHostUrl());
    }

    public List<WizardTransition> getAvailableActions()
    {
        List<WizardTransition> transitions = super.getAvailableActions();
        transitions.remove(WizardTransition.CANCEL);
        return transitions;        
    }

    public Type getType()
    {
        return adminConfigType;
    }

    public void doFinish()
    {
        super.doFinish();

        try
        {
            UserConfiguration adminUser = saveConfiguration();

            // login as the admin user.  safe to directly create AcegiUser as
            // we know the user has no external authorities
            AcegiUtils.loginAs(new AcegiUser(userManager.getUser(adminUser.getLogin()), Collections.EMPTY_LIST));

            try
            {
                // ensure that this runs in a separate thread so that the
                // use can receive appropriate feedback.
                threadFactory.newThread(new Runnable()
                {
                    public void run()
                    {
                        setupManager.requestSetupComplete(true);
                    }
                }).start();
            }
            catch (Exception e)
            {
                LOG.severe(e);
            }
        }
        catch (TypeException e)
        {
            LOG.severe(e);
        }

    }

    private UserConfiguration saveConfiguration() throws TypeException
    {
        AcegiUtils.loginAsSystem();
        
        try
        {
            SimpleInstantiator instantiator = new SimpleInstantiator(configurationReferenceManager);
            AdminUserConfiguration adminConfig = (AdminUserConfiguration) instantiator.instantiate(adminConfigType, getCompletedStateForType(adminConfigType).getDataRecord());
            MutableRecord serverConfigRecord = getCompletedStateForType(serverConfigType).getDataRecord();

            // create the admin user.
            UserConfiguration adminUser = new UserConfiguration();
            adminUser.setPermanent(true);
            adminUser.setLogin(adminConfig.getLogin());
            adminUser.setName(adminConfig.getName());
            adminUser.setPassword(passwordEncoder.encodePassword(adminConfig.getPassword(), null));
            adminUser.addDirectAuthority(ServerPermission.ADMINISTER.toString());
            configurationTemplateManager.insert(ConfigurationRegistry.USERS_SCOPE, adminUser);

            // Special all-users group.
            BuiltinGroupConfiguration allUsersGroup = new BuiltinGroupConfiguration(UserManager.ALL_USERS_GROUP_NAME, GrantedAuthority.USER);
            allUsersGroup.setPermanent(true);
            configurationTemplateManager.insert(ConfigurationRegistry.GROUPS_SCOPE, allUsersGroup);

            // Special anonymous users group.
            BuiltinGroupConfiguration anonymousUsersGroup = new BuiltinGroupConfiguration(UserManager.ANONYMOUS_USERS_GROUP_NAME, GrantedAuthority.GUEST);
            anonymousUsersGroup.setPermanent(true);
            configurationTemplateManager.insert(ConfigurationRegistry.GROUPS_SCOPE, anonymousUsersGroup);
            
            // create an administrators group (for convenience)
            GroupConfiguration adminGroup = new GroupConfiguration(UserManager.ADMINS_GROUP_NAME);
            adminGroup.addServerPermission(ServerPermission.ADMINISTER);
            adminGroup.addServerPermission(ServerPermission.PERSONAL_BUILD);
            configurationTemplateManager.insert(ConfigurationRegistry.GROUPS_SCOPE, adminGroup);

            // and a project admins group that has admin access to all projects
            GroupConfiguration projectAdmins = new GroupConfiguration(UserManager.PROJECT_ADMINS_GROUP_NAME);
            projectAdmins.addServerPermission(ServerPermission.PERSONAL_BUILD);
            projectAdmins.addServerPermission(ServerPermission.CREATE_PROJECT);
            projectAdmins.addServerPermission(ServerPermission.DELETE_PROJECT);
            configurationTemplateManager.insert(ConfigurationRegistry.GROUPS_SCOPE, projectAdmins);

            // and a developers group that has personal build access (for convenience)
            GroupConfiguration developersGroup = new GroupConfiguration(UserManager.DEVELOPERS_GROUP_NAME);
            developersGroup.addServerPermission(ServerPermission.PERSONAL_BUILD);
            configurationTemplateManager.insert(ConfigurationRegistry.GROUPS_SCOPE, developersGroup);

            // apply the settings
            CompositeType generalType = typeRegistry.getType(GeneralAdminConfiguration.class);
            MutableRecord record = generalType.createNewRecord(true);
            record.setPermanent(true);
            String baseUrl = (String) serverConfigRecord.get("baseUrl");
            record.put("baseUrl", baseUrl);
            record.put("masterHost", getMasterHost(baseUrl));
            configurationTemplateManager.saveRecord(PathUtils.getPath(GlobalConfiguration.SCOPE_NAME, "generalConfig"), record);

            // Now copy over the email properties
            extractAndSave(EmailConfiguration.class, serverConfigRecord, true);
            return adminUser;
        }
        finally
        {
            AcegiUtils.logout();
        }
    }

    private String getMasterHost(String baseUrl)
    {
        String masterHost = null;
        if(baseUrl != null)
        {
            // Pull out just the host part
            try
            {
                URL url = new URL(baseUrl);
                masterHost = url.getHost();
            }
            catch (MalformedURLException e)
            {
                // Nice try
            }
        }

        if(masterHost == null)
        {
            // So much for that plan...let's try and get the host name
            try
            {
                InetAddress address = InetAddress.getLocalHost();
                masterHost = address.getCanonicalHostName();
            }
            catch (UnknownHostException e)
            {
                // Oh well, we tried
                masterHost = "localhost";
            }
        }

        return masterHost;
    }

    private void extractAndSave(Class clazz, MutableRecord wizardRecord, boolean permanent)
    {
        CompositeType type = typeRegistry.getType(clazz);

        MutableRecord record = type.createNewRecord(true);
        record.setPermanent(permanent);
        for(TypeProperty property: type.getProperties())
        {
            String name = property.getName();
            Object value = wizardRecord.get(name);
            if(value != null)
            {
                record.put(name, value);
            }
        }

        List<String> paths = configurationPersistenceManager.getConfigurationPaths(type);
        assert(paths.size() == 1);
        String path = paths.get(0);
        configurationTemplateManager.saveRecord(path, record);
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setSetupManager(SetupManager setupManager)
    {
        this.setupManager = setupManager;
    }

    public void setConfigurationReferenceManager(ConfigurationReferenceManager configurationReferenceManager)
    {
        this.configurationReferenceManager = configurationReferenceManager;
    }

    public void setThreadFactory(ThreadFactory threadFactory)
    {
        this.threadFactory = threadFactory;
    }

    public void setPasswordEncoder(PasswordEncoder passwordEncoder)
    {
        this.passwordEncoder = passwordEncoder;
    }
}