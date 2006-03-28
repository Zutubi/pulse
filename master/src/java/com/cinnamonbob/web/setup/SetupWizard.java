package com.cinnamonbob.web.setup;

import com.cinnamonbob.bootstrap.ApplicationConfiguration;
import com.cinnamonbob.bootstrap.ConfigurationManager;
import com.cinnamonbob.bootstrap.SetupManager;
import com.cinnamonbob.model.GrantedAuthority;
import com.cinnamonbob.model.User;
import com.cinnamonbob.model.UserManager;
import com.cinnamonbob.security.AcegiUtils;
import com.cinnamonbob.web.DefaultAction;
import com.cinnamonbob.web.wizard.BaseWizard;
import com.cinnamonbob.web.wizard.BaseWizardState;
import com.cinnamonbob.web.wizard.Wizard;
import com.cinnamonbob.web.wizard.WizardCompleteState;
import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.Validateable;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * <class-comment/>
 */
public class SetupWizard extends BaseWizard
{
    private CreateAdminState createAdminState;

    private UserManager userManager;
    private ConfigurationManager configurationManager;
    private ServerSettingsState serverSettingsState;
    private SetupManager setupManager;

    public SetupWizard()
    {
        // create the admin user.
        createAdminState = new CreateAdminState(this, "admin");
        serverSettingsState = new ServerSettingsState(this, "settings");

        addInitialState("admin", createAdminState);
        addState("settings", serverSettingsState);
        addFinalState("success", new WizardCompleteState(this, "success"));
    }

    public void process()
    {
        super.process();

        // create the admin user.
        User admin = createAdminState.getAdmin();
        admin.setEnabled(true);
        admin.add(GrantedAuthority.USER);
        admin.add(GrantedAuthority.ADMINISTRATOR);
        // Send the admin to a welcome page by default
        admin.setDefaultAction(DefaultAction.WELCOME_ACTION);
        userManager.save(admin);

        // apply the settings
        ApplicationConfiguration config = configurationManager.getAppConfig();
        config.setHostName(serverSettingsState.getHostname());
        config.setSmtpFrom(serverSettingsState.getFromAddress());
        config.setSmtpHost(serverSettingsState.getSmtpHost());
        config.setSmtpUsername(serverSettingsState.getUsername());
        config.setSmtpPassword(serverSettingsState.getPassword());

        // login as the admin user.
        AcegiUtils.loginAs(admin);

        try
        {
            setupManager.setupComplete();
        }
        catch (Exception e)
        {
            e.printStackTrace();
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
    public void setConfigurationManager(ConfigurationManager configurationManager)
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

    public class CreateAdminState extends BaseWizardState implements Validateable
    {
        private User admin = new User();

        private String confirm;

        public CreateAdminState(Wizard wizard, String name)
        {
            super(wizard, name);
        }

        public void validate()
        {
            if (!confirm.equals(admin.getPassword()))
            {
                addFieldError("confirm", "confirmed password does not match password, please re-enter your password");
            }
        }

        public String getNextStateName()
        {
            return "settings";
        }

        public User getAdmin()
        {
            return admin;
        }

        public String getConfirm()
        {
            return confirm;
        }

        public void setConfirm(String confirm)
        {
            this.confirm = confirm;
        }
    }

    public class ServerSettingsState extends BaseWizardState implements Validateable
    {
        private String hostname;
        private String fromAddress;
        private String smtpHost;
        private String username;
        private String password;

        public ServerSettingsState(Wizard wizard, String name)
        {
            super(wizard, name);
        }

        public void initialise()
        {
            try
            {
                InetAddress address = InetAddress.getLocalHost();
                hostname = address.getCanonicalHostName() + ":" + configurationManager.getAppConfig().getServerPort();
            }
            catch (UnknownHostException e)
            {
                // Oh well, we tried
            }

        }

        public String getNextStateName()
        {
            return "success";
        }

        public String getHostname()
        {
            return hostname;
        }

        public void setHostname(String hostname)
        {
            this.hostname = hostname;
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
            if (TextUtils.stringSet(smtpHost) && !TextUtils.stringSet(fromAddress))
            {
                addFieldError("fromAddress", "from address is required when smtp host is provided");
            }
        }
    }
}
