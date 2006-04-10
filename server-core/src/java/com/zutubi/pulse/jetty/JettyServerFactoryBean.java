package com.zutubi.pulse.jetty;

import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.util.logging.Logger;
import org.mortbay.http.SocketListener;
import org.mortbay.jetty.Server;
import org.springframework.beans.factory.FactoryBean;

/**
 * 
 *
 */
public class JettyServerFactoryBean implements FactoryBean
{
    private static final Logger LOG = Logger.getLogger(JettyServerFactoryBean.class);

    private static Server instance;

    private ConfigurationManager configManager = null;

    public Object getObject() throws Exception
    {
        if (instance == null)
        {
            synchronized(this)
            {
                if (instance == null)
                {
                    instance = new Server();

                    // configuration of the server depends upon the configmanager.
                    SocketListener listener = new SocketListener();
                    listener.setPort(configManager.getAppConfig().getServerPort());
                    instance.addListener(listener);
                }
            }
        }
        return instance;
    }

    public Class getObjectType()
    {
        return Server.class;
    }

    public boolean isSingleton()
    {
        return true;
    }

    public void setConfigurationManager(ConfigurationManager configManager)
    {
        this.configManager = configManager;
    }

    public void shutdown()
    {
        try
        {
            if (instance.isStarted())
            {
                instance.stop();
            }
        } 
        catch (InterruptedException e)
        {
            LOG.error(e);
        }
    }
}

