package com.cinnamonbob.bootstrap.jetty;

import com.cinnamonbob.bootstrap.ConfigurationManager;
import org.mortbay.jetty.Server;
import org.mortbay.http.SocketListener;
import org.springframework.beans.factory.FactoryBean;

import java.util.logging.Logger;

/**
 * 
 *
 */
public class JettyServerFactoryBean implements FactoryBean
{

    private static final Logger LOG = Logger.getLogger(JettyServerFactoryBean.class.getName());

    private static Server SERVER;

    private ConfigurationManager configManager = null;

    public Object getObject() throws Exception
    {
        if (SERVER == null)
        {
            synchronized(this)
            {
                if (SERVER == null)
                {
                    SERVER = new Server();

                    // configuration of the server depends upon the configmanager.
                    SocketListener listener = new SocketListener();
                    listener.setPort(configManager.getAppConfig().getServerPort());
                    SERVER.addListener(listener);

                }
            }
        }
        return SERVER;
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
            SERVER.stop();
        } 
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}

