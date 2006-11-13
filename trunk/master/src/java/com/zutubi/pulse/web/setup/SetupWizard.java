package com.zutubi.pulse.web.setup;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.Validateable;
import com.opensymphony.xwork.validator.ValidationException;
import com.opensymphony.xwork.validator.DelegatingValidatorContext;
import com.zutubi.pulse.bootstrap.MasterConfiguration;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.bootstrap.SetupManager;
import com.zutubi.pulse.bootstrap.SystemConfigurationSupport;
import com.zutubi.pulse.model.GrantedAuthority;
import com.zutubi.pulse.model.Group;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.model.UserManager;
import com.zutubi.pulse.security.AcegiUtils;
import com.zutubi.pulse.util.logging.Logger;
import com.zutubi.pulse.web.DefaultAction;
import com.zutubi.pulse.web.wizard.BaseWizard;
import com.zutubi.pulse.web.wizard.BaseWizardState;
import com.zutubi.pulse.web.wizard.Wizard;
import com.zutubi.pulse.xwork.validator.validators.EmailValidator;

/**
 * <class-comment/>
 */
public class SetupWizard extends BaseWizard
{
    private static final Logger LOG = Logger.getLogger(SetupWizard.class);

    private CreateAdminState createAdminState;
    private ServerSettingsState serverSettingsState;

    private MasterConfigurationManager configurationManager;
    private UserManager userManager;
    private SetupManager setupManager;

    public SetupWizard()
    {
        // create the admin user.
        createAdminState = new CreateAdminState(this, "admin");
        serverSettingsState = new ServerSettingsState(this, "settings");

        addInitialState(createAdminState);
        addState(serverSettingsState);
    }

    public void process()
    {
        super.process();

        MasterConfiguration config = configurationManager.getAppConfig();

        // create the admin user.
        User admin = createAdminState.getAdmin();
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
        config.setBaseUrl(serverSettingsState.getBaseUrl());
        config.setSmtpFrom(serverSettingsState.getFromAddress());
        config.setSmtpHost(serverSettingsState.getSmtpHost());
        config.setSmtpUsername(serverSettingsState.getUsername());
        config.setSmtpPassword(serverSettingsState.getPassword());
        config.setSmtpPrefix(serverSettingsState.getPrefix());

        // login as the admin user.
        AcegiUtils.loginAs(admin);

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
            addActionError(e.getMessage());
            LOG.severe(e.getMessage(), e);
        }
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

    /**
     * The create admin user page.
     */
    public class CreateAdminState extends BaseWizardState implements Validateable
    {
        private User admin = new User();

        /**
         * The password confirmation field, must match the admin.getPassword() value
         */
        private String confirm;

        public CreateAdminState(Wizard wizard, String name)
        {
            super(wizard, name);
        }

        public void validate()
        {
            if (!confirm.equals(admin.getPassword()))
            {
                addFieldError("confirm", getTextProvider().getText("admin.password.confirm.mismatch"));
            }
        }

        /**
         * The next page is always the settings page.
         *
         * @return the server settings state name.
         */
        public String getNextStateName()
        {
            return serverSettingsState.getStateName();
        }

        public User getAdmin()
        {
            return admin;
        }

        /**
         * Getter for the password confirmation field.
         *
         * @return current value.
         */
        public String getConfirm()
        {
            return confirm;
        }

        /**
         * Setter for the password confirmation field.
         *
         * @param confirm
         */
        public void setConfirm(String confirm)
        {
            this.confirm = confirm;
        }
    }

    public class ServerSettingsState extends BaseWizardState implements Validateable
    {
        private String baseUrl;
        private String fromAddress;
        private String smtpHost;
        private String username;
        private String password;
        private String prefix;

        public ServerSettingsState(Wizard wizard, String name)
        {
            super(wizard, name);
        }

        public void initialise()
        {
            SystemConfigurationSupport systemConfig = (SystemConfigurationSupport) configurationManager.getSystemConfig();
            baseUrl = systemConfig.getHostUrl();
        }

        public String getNextStateName()
        {
            return "success";
        }

        public String getBaseUrl()
        {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl)
        {
            this.baseUrl = baseUrl;
        }

        public String getFromAddress()
        {
            return fromAddress;
        }

        public void setFromAddress(String fromAddress)
        {
            this.fromAddress = fromAddress;
        }

        public String getSmtpHost()
        {
            return smtpHost;
        }

        public void setSmtpHost(String smtpHost)
        {
            this.smtpHost = smtpHost;
        }

        public String getUsername()
        {
            return username;
        }

        public void setUsername(String username)
        {
            this.username = username;
        }

        public String getPassword()
        {
            return password;
        }

        public void setPassword(String password)
        {
            this.password = password;
        }

        public void validate()
        {
            if (TextUtils.stringSet(smtpHost))
            {
                if (!TextUtils.stringSet(fromAddress))
                {
                    addFieldError("fromAddress", "from address is required when smtp host is provided");
                }
            }
            
            // If the from address is specified, then ensure that a valid value is set.
            if (TextUtils.stringSet(fromAddress))
            {
                EmailValidator validator = new EmailValidator();
                validator.setValidatorContext(new DelegatingValidatorContext(this));
                validator.setFieldName("fromAddress");
                try
                {
                    validator.validate(this);
                }
                catch (ValidationException e)
                {
                    addFieldError("fromAddress", e.getMessage());
                }
            }
        }

        public String getPrefix()
        {
            return prefix;
        }

        public void setPrefix(String prefix)
        {
            this.prefix = prefix;
        }
    }
}
