/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.setup;

import com.zutubi.pulse.bootstrap.ApplicationConfiguration;
import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.bootstrap.SetupManager;
import com.zutubi.pulse.model.GrantedAuthority;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.model.UserManager;
import com.zutubi.pulse.security.AcegiUtils;
import com.zutubi.pulse.web.DefaultAction;
import com.zutubi.pulse.web.wizard.BaseWizard;
import com.zutubi.pulse.web.wizard.BaseWizardState;
import com.zutubi.pulse.web.wizard.Wizard;
import com.zutubi.pulse.web.wizard.WizardCompleteState;
import com.zutubi.pulse.license.LicenseDecoder;
import com.zutubi.pulse.license.License;
import com.zutubi.pulse.license.LicenseException;
import com.zutubi.pulse.util.logging.Logger;
import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.Validateable;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.IOException;

/**
 * <class-comment/>
 */
public class SetupWizard extends BaseWizard
{
    private static final Logger LOG = Logger.getLogger(SetupWizard.class);

    private LicenseState licenseState;
    private CreateAdminState createAdminState;
    private ServerSettingsState serverSettingsState;

    private ConfigurationManager configurationManager;
    private UserManager userManager;
    private SetupManager setupManager;

    public SetupWizard()
    {
        // create the admin user.
        licenseState = new LicenseState(this, "license");
        createAdminState = new CreateAdminState(this, "admin");
        serverSettingsState = new ServerSettingsState(this, "settings");

        addInitialState("license", licenseState);
        addState("admin", createAdminState);
        addState("settings", serverSettingsState);
        addFinalState("success", new WizardCompleteState(this, "success"));
    }

    public void process()
    {
        super.process();

        // record the license details.
        String licenseKey = licenseState.getLicense();
        licenseKey = licenseKey.replaceAll("\n", "");
        try
        {
            configurationManager.getHome().updateLicenseKey(licenseKey);
        }
        catch (IOException e)
        {
            addActionError(e.getMessage());
            LOG.severe(e.getMessage(), e);
            return;
        }

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
        config.setSmtpPrefix(serverSettingsState.getPrefix());

        // login as the admin user.
        AcegiUtils.loginAs(admin);

        try
        {
            setupManager.setupComplete();
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

    public class LicenseState extends BaseWizardState implements Validateable
    {
        private String license;

        public LicenseState(Wizard wizard, String name)
        {
            super(wizard, name);
        }

        public String getLicense()
        {
            return license;
        }

        public void setLicense(String license)
        {
            this.license = license;
        }

        public String getNextStateName()
        {
            return createAdminState.getStateName();
        }

        public void validate()
        {
            // take the license string, strip out any '\n' chars and check it.
            String licenseKey = license.replaceAll("\n", "");
            LicenseDecoder decoder = new LicenseDecoder();
            try
            {
                License l = decoder.decode(licenseKey.getBytes());
                if (l == null)
                {
                    addFieldError("license", getTextProvider().getText("license.invalid"));
                }
            }
            catch (LicenseException e)
            {
                addFieldError("license", getTextProvider().getText("license.decode.error"));
            }
        }
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
        private String hostname;
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
