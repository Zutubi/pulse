package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.bootstrap.MasterConfiguration;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.jabber.JabberManager;
import com.zutubi.pulse.web.ActionSupport;

/**
 * Action to configure connection to a Jabber server for instant message
 * notifications.
 */
public class JabberConfigurationAction extends ActionSupport
{
    private MasterConfigurationManager configurationManager;

    private JabberConfig jabber = new JabberConfig();
    private JabberManager jabberManager;

    public JabberConfig getJabber()
    {
        return jabber;
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

    public String doRefresh()
    {
        jabberManager.refresh();
        return SUCCESS;
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
        config.setJabberHost(null);
        config.setJabberPort(JabberManager.DEFAULT_PORT);
        config.setJabberUsername(null);
        config.setJabberPassword(null);
        config.setJabberForceSSL(Boolean.FALSE);
    }

    private void saveConfig()
    {
        MasterConfiguration config = configurationManager.getAppConfig();
        config.setJabberHost(jabber.getHost());
        config.setJabberPort(jabber.getPort());
        config.setJabberUsername(jabber.getUsername());
        config.setJabberPassword(jabber.getPassword());
        config.setJabberForceSSL(jabber.getForceSSL());
    }

    private void loadConfig()
    {
        MasterConfiguration config = configurationManager.getAppConfig();
        jabber.setHost(config.getJabberHost());
        jabber.setPort(config.getJabberPort());
        jabber.setUsername(config.getJabberUsername());
        jabber.setPassword(config.getJabberPassword());
        jabber.setForceSSL(config.getJabberForceSSL());
    }

    /**
     * Required resource, provides access to the Jabber configuration details.
     *
     * @param config
     */
    public void setConfigurationManager(MasterConfigurationManager config)
    {
        this.configurationManager = config;
    }

    public void setJabberManager(JabberManager jabberManager)
    {
        this.jabberManager = jabberManager;
    }

    /**
     * Holder for the form post.
     */
    public class JabberConfig
    {
        private String host;
        private int port = JabberManager.DEFAULT_PORT;
        private String username;
        private String password;
        private Boolean forceSSL;

        public String getHost()
        {
            return host;
        }

        public void setHost(String host)
        {
            this.host = host;
        }

        public int getPort()
        {
            return port;
        }

        public void setPort(int port)
        {
            this.port = port;
        }

        public String getPassword()
        {
            return password;
        }

        public void setPassword(String password)
        {
            this.password = password;
        }

        public String getUsername()
        {
            return username;
        }

        public void setUsername(String username)
        {
            this.username = username;
        }

        public Boolean getForceSSL()
        {
            return forceSSL;
        }

        public void setForceSSL(Boolean forceSSL)
        {
            this.forceSSL = forceSSL;
        }
    }
}
