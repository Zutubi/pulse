package com.zutubi.pulse.prototype.config.setup;

import com.zutubi.prototype.config.ConfigurationPersistenceManager;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.wizard.WizardState;
import com.zutubi.prototype.wizard.WizardTransition;
import com.zutubi.prototype.wizard.webwork.AbstractTypeWizard;
import com.zutubi.pulse.bootstrap.MasterConfiguration;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.bootstrap.SetupManager;
import com.zutubi.pulse.bootstrap.SystemConfigurationSupport;
import com.zutubi.pulse.model.AcegiUser;
import com.zutubi.pulse.model.GrantedAuthority;
import com.zutubi.pulse.model.Group;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.model.UserManager;
import com.zutubi.pulse.security.AcegiUtils;
import com.zutubi.pulse.web.DefaultAction;

import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public class SetupWizard extends AbstractTypeWizard
{
    private ConfigurationPersistenceManager configurationPersistenceManager;

    private UserManager userManager;

    private MasterConfigurationManager configurationManager;

    private SetupManager setupManager;

    private CompositeType adminConfigType;
    private CompositeType serverConfigType;

    public void initialise()
    {
        adminConfigType = (CompositeType) configurationPersistenceManager.getType("setup/admin");
        serverConfigType = (CompositeType) configurationPersistenceManager.getType("setup/server");

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
            ServerSettingsConfiguration serverConfig = (ServerSettingsConfiguration) serverConfigType.instantiate(null, wizardStates.get(1).getRecord());

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
            userManager.setPassword(admin, admin.getPassword());
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
            config.setBaseUrl(serverConfig.getBaseUrl());
            config.setSmtpFrom(serverConfig.getFromAddress());
            config.setSmtpHost(serverConfig.getSmtpHost());
            config.setSmtpSSL(serverConfig.getSmtpSSL());
            if(serverConfig.getSmtpCustomPort())
            {
                config.setSmtpPort(serverConfig.getSmtpPort());
            }
            config.setSmtpSSL(serverConfig.getSmtpSSL());
            config.setSmtpUsername(serverConfig.getUsername());
            config.setSmtpPassword(serverConfig.getPassword());
            config.setSmtpPrefix(serverConfig.getPrefix());

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

    /**
     * Required resource.
     *
     * @param configurationPersistenceManager
     *         instance
     */
    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }

    /**
     * Required resource.
     *
     * @param userManager
     */
    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    /**
     * Required resource.
     *
     * @param configurationManager
     */
    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    /**
     * Required resource.
     *
     * @param setupManager
     */
    public void setSetupManager(SetupManager setupManager)
    {
        this.setupManager = setupManager;
    }

}