package com.zutubi.pulse.prototype.config.setup;

import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.TypeProperty;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.wizard.WizardState;
import com.zutubi.prototype.wizard.WizardTransition;
import com.zutubi.prototype.wizard.webwork.AbstractTypeWizard;
import com.zutubi.pulse.bootstrap.MasterConfiguration;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.bootstrap.SetupManager;
import com.zutubi.pulse.bootstrap.SystemConfigurationSupport;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.prototype.config.admin.EmailConfiguration;
import com.zutubi.pulse.prototype.config.admin.GlobalConfiguration;
import com.zutubi.pulse.security.AcegiUtils;
import com.zutubi.pulse.web.DefaultAction;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class SetupWizard extends AbstractTypeWizard
{
    private static final String GENERAL_CONFIG_PROPERTY = "generalConfig";

    private CompositeType adminConfigType;
    
    private UserManager userManager;
    private MasterConfigurationManager configurationManager;
    private SetupManager setupManager;

    public void initialise()
    {
        adminConfigType = typeRegistry.getType(AdminUserConfiguration.class);
        CompositeType serverConfigType = typeRegistry.getType(ServerSettingsConfiguration.class);

        wizardStates = new LinkedList<WizardState>();
        addWizardStates(wizardStates, adminConfigType, null);
        addWizardStates(wizardStates, serverConfigType, null);

        // a bit of custom initialisation
        SystemConfigurationSupport systemConfig = (SystemConfigurationSupport) configurationManager.getSystemConfig();
        
        MutableRecord record = wizardStates.get(1).getRecord();
        record.put("baseUrl", systemConfig.getHostUrl());

        currentState = wizardStates.getFirst();
    }

    public List<WizardTransition> getAvailableActions()
    {
        List<WizardTransition> transitions = super.getAvailableActions();
        transitions.remove(WizardTransition.CANCEL);
        return transitions;        
    }

    public void doFinish()
    {
        try
        {
            AdminUserConfiguration adminConfig = (AdminUserConfiguration) adminConfigType.instantiate(null, wizardStates.get(0).getRecord());
            MutableRecord serverConfigRecord = wizardStates.get(1).getRecord();
            MasterConfiguration config = configurationManager.getAppConfig();

            // create the admin user.
            User admin = new User();
            admin.setLogin(adminConfig.getLogin());
            admin.setName(adminConfig.getName());
            userManager.setPassword(admin, adminConfig.getPassword());

            admin.setEnabled(true);
            admin.add(GrantedAuthority.USER);
            admin.add(GrantedAuthority.ADMINISTRATOR);
            config.setAdminLogin(admin.getLogin());

            // Send the admin to a welcome page by default
            admin.setDefaultAction(DefaultAction.WELCOME_ACTION);
            userManager.save(admin);

            // make sure that we encode the password after we have a persistent user,
            // since the users id is required. This is a little awkward...
            userManager.setPassword(admin, adminConfig.getPassword());
            userManager.save(admin);

            // create an administrators group (for convenience)
            Group adminGroup = new Group("administrators");
            adminGroup.addAdditionalAuthority(GrantedAuthority.ADMINISTRATOR);
            adminGroup.addAdditionalAuthority(GrantedAuthority.PERSONAL);
            userManager.addGroup(adminGroup);

            // and a project admins group that has write access to all projects
            Group projectAdmins = new Group("project administrators");
            projectAdmins.addAdditionalAuthority(GrantedAuthority.PERSONAL);
            projectAdmins.setAdminAllProjects(true);
            userManager.addGroup(projectAdmins);

            // and a developers group that has personal build access (for convenience)
            Group developersGroup = new Group("developers");
            developersGroup.addAdditionalAuthority(GrantedAuthority.PERSONAL);
            userManager.addGroup(developersGroup);

            // apply the settings
            CompositeType generalType = typeRegistry.getType(GENERAL_CONFIG_PROPERTY);
            MutableRecord record = generalType.createNewRecord();
            String baseUrl = (String) serverConfigRecord.get("baseUrl");
            record.put("baseUrl", baseUrl);
            record.put("masterHost", getMasterHost(baseUrl));
            configurationPersistenceManager.saveRecord(GlobalConfiguration.SCOPE_NAME, GENERAL_CONFIG_PROPERTY, record);

            // Now copy over the email properties
            extractAndSave(EmailConfiguration.class, serverConfigRecord);

            // login as the admin user.  safe to directly create AcegiUser as
            // we know the user has no external authorities
            AcegiUtils.loginAs(new AcegiUser(admin));

            try
            {
                // ensure that this runs in a separate thread so that the
                // use can receive appropriate feedback.
                new Thread(new Runnable()
                {
                    public void run()
                    {
                        setupManager.requestSetupComplete();
                    }
                }).start();
            }
            catch (Exception e)
            {
                e.printStackTrace();
/*
            addActionError(e.getMessage());
            LOG.severe(e.getMessage(), e);
*/
            }
        }
        catch (TypeException e)
        {
            e.printStackTrace();
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

    private void extractAndSave(Class clazz, MutableRecord wizardRecord)
    {
        CompositeType type = typeRegistry.getType(clazz);

        MutableRecord record = type.createNewRecord();
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
        configurationPersistenceManager.saveRecord(PathUtils.getParentPath(path), PathUtils.getBaseName(path), record);
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

}