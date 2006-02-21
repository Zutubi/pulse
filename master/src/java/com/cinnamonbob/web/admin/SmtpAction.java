package com.cinnamonbob.web.admin;

import com.cinnamonbob.web.ActionSupport;
import com.cinnamonbob.bootstrap.ConfigurationManager;
import com.cinnamonbob.bootstrap.ApplicationConfiguration;

/**
 * <class-comment/>
 */
public class SmtpAction extends ActionSupport
{
    private ConfigurationManager configurationManager;

    private SmtpConfig smtp = new SmtpConfig();

    public SmtpConfig getSmtp()
    {
        return smtp;
    }

    public String doSave()
    {
        return SUCCESS;
    }

    public String doInput()
    {
        ApplicationConfiguration config = configurationManager.getAppConfig();

        // load the smtp details.
        smtp.setPrefix("[BOB]");
        smtp.setFrom(config.getSmtpFrom());
        smtp.setHost(config.getSmtpHost());

        return INPUT;
    }

    public String execute()
    {
        // default action, load the config details.

        return SUCCESS;
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
    private class SmtpConfig
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
