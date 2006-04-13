/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.bootstrap.ApplicationConfiguration;
import com.zutubi.pulse.jabber.JabberManager;
import org.hibernate.event.EventSource;

/**
 * <class-comment/>
 */
public class JabberConfigurationAction extends ActionSupport
{
    private ConfigurationManager configurationManager;

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

    public String execute()
    {
        // default action, load the config details.
        loadConfig();

        return SUCCESS;
    }

    private void resetConfig()
    {
        ApplicationConfiguration config = configurationManager.getAppConfig();
        config.setJabberHost(null);
        config.setJabberPort(JabberManager.DEFAULT_PORT);
        config.setJabberUsername(null);
        config.setJabberPassword(null);
        jabberManager.refresh();
    }

    private void saveConfig()
    {
        ApplicationConfiguration config = configurationManager.getAppConfig();
        config.setJabberHost(jabber.getHost());
        config.setJabberPort(jabber.getPort());
        config.setJabberUsername(jabber.getUsername());
        config.setJabberPassword(jabber.getPassword());
        jabberManager.refresh();
    }

    private void loadConfig()
    {
        ApplicationConfiguration config = configurationManager.getAppConfig();
        jabber.setHost(config.getJabberHost());
        jabber.setUsername(config.getJabberUsername());
        jabber.setPassword(config.getJabberPassword());
    }

    /**
     * Required resource, provides access to the Jabber configuration details.
     *
     * @param config
     */
    public void setConfigurationManager(ConfigurationManager config)
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
    }
}
