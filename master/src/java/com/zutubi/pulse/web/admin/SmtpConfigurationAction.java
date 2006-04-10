package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.bootstrap.ApplicationConfiguration;

/**
 * <class-comment/>
 */
public class SmtpConfigurationAction extends ActionSupport
{
    private ConfigurationManager configurationManager;

    private SmtpConfig smtp = new SmtpConfig();

    public SmtpConfig getSmtp()
    {
        return smtp;
    }

    public String doReset()
    {
        resetConfig();
        loadConfig();
        return SUCCESS;
    }

    public String doSave()
    {
        saveConfig();

        return SUCCESS;
    }

    public String doInput()
    {
        loadConfig();

        return INPUT;
    }

    public String execute()
    {
        // default action, load the config details.
        loadConfig();

        return SUCCESS;
    }

    private void resetConfig()
    {
        ApplicationConfiguration config = configurationManager.getAppConfig();
        config.setSmtpPrefix(null);
        config.setSmtpFrom(null);
        config.setSmtpHost(null);
        config.setSmtpUsername(null);
        config.setSmtpPassword(null);
    }

    private void saveConfig()
    {
        ApplicationConfiguration config = configurationManager.getAppConfig();
        config.setSmtpPrefix(smtp.getPrefix());
        config.setSmtpFrom(smtp.getFrom());
        config.setSmtpHost(smtp.getHost());
        config.setSmtpUsername(smtp.getUsername());
        config.setSmtpPassword(smtp.getPassword());
    }

    private void loadConfig()
    {
        ApplicationConfiguration config = configurationManager.getAppConfig();
        smtp.setPrefix(config.getSmtpPrefix());
        smtp.setFrom(config.getSmtpFrom());
        smtp.setHost(config.getSmtpHost());
        smtp.setUsername(config.getSmtpUsername());
        smtp.setPassword(config.getSmtpPassword());
    }

    /**
     * Required resource, provides access to the SMTP configuration details.
     *
     * @param config
     */
    public void setConfigurationManager(ConfigurationManager config)
    {
        this.configurationManager = config;
    }

    /**
     * Holder for the form post.
     */
    public class SmtpConfig
    {
        private String from;
        private String prefix;
        private String host;
        private String username;
        private String password;

        public String getFrom()
        {
            return from;
        }

        public void setFrom(String from)
        {
            this.from = from;
        }

        public String getHost()
        {
            return host;
        }

        public void setHost(String host)
        {
            this.host = host;
        }

        public String getPassword()
        {
            return password;
        }

        public void setPassword(String password)
        {
            this.password = password;
        }

        public String getPrefix()
        {
            return prefix;
        }

        public void setPrefix(String prefix)
        {
            this.prefix = prefix;
        }

        public String getUsername()
        {
            return username;
        }

        public void setUsername(String username)
        {
            this.username = username;
        }
    }
}
