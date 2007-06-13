package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.bootstrap.MasterConfiguration;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.web.ActionSupport;

/**
 * <class-comment/>
 */
public class SmtpConfigurationAction extends ActionSupport
{
    private MasterConfigurationManager configurationManager;

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
        MasterConfiguration config = configurationManager.getAppConfig();
        config.setSmtpPrefix(null);
        config.setSmtpFrom(null);
        config.setSmtpHost(null);
        config.setSmtpSSL(false);
        config.setSmtpPort(-1);
        config.setSmtpUsername(null);
        config.setSmtpPassword(null);
        config.setSmtpLocalhost(null);
    }

    private void saveConfig()
    {
        MasterConfiguration config = configurationManager.getAppConfig();
        config.setSmtpPrefix(smtp.getPrefix());
        config.setSmtpFrom(smtp.getFrom());
        config.setSmtpHost(smtp.getHost());
        config.setSmtpSSL(smtp.getSsl());
        config.setSmtpUsername(smtp.getUsername());
        config.setSmtpPassword(smtp.getPassword());
        config.setSmtpLocalhost(smtp.getLocalhost());

        if(smtp.getCustomPort())
        {
            config.setSmtpPort(smtp.getPort());
        }
        else
        {
            config.setSmtpPort(-1);
        }
    }

    private void loadConfig()
    {
        MasterConfiguration config = configurationManager.getAppConfig();
        smtp.setPrefix(config.getSmtpPrefix());
        smtp.setFrom(config.getSmtpFrom());
        smtp.setHost(config.getSmtpHost());
        smtp.setSsl(config.getSmtpSSL());
        smtp.setUsername(config.getSmtpUsername());
        smtp.setPassword(config.getSmtpPassword());
        smtp.setLocalhost(config.getSmtpLocalhost());
        int port = config.getSmtpPort();

        if(port > 0)
        {
            smtp.setCustomPort(true);
            smtp.setPort(port);
        }
        else
        {
            smtp.setCustomPort(false);
            smtp.setPort(config.getSmtpSSL() ? 465 : 25);
        }
    }

    /**
     * Required resource, provides access to the SMTP configuration details.
     *
     * @param config
     */
    public void setConfigurationManager(MasterConfigurationManager config)
    {
        this.configurationManager = config;
    }

    /**
     * Holder for the form post.
     */
    public class SmtpConfig
    {
        private String from;
        private boolean ssl;
        private String prefix;
        private String host;
        private String username;
        private String password;
        private String localhost;
        private boolean customPort;
        private int port;

        public String getFrom()
        {
            return from;
        }

        public void setFrom(String from)
        {
            this.from = from;
        }

        public boolean getSsl()
        {
            return ssl;
        }

        public void setSsl(boolean ssl)
        {
            this.ssl = ssl;
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

        public String getLocalhost()
        {
            return localhost;
        }

        public void setLocalhost(String localhost)
        {
            this.localhost = localhost;
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

        public boolean getCustomPort()
        {
            return customPort;
        }

        public void setCustomPort(boolean customPort)
        {
            this.customPort = customPort;
        }

        public int getPort()
        {
            return port;
        }

        public void setPort(int port)
        {
            this.port = port;
        }
    }
}
