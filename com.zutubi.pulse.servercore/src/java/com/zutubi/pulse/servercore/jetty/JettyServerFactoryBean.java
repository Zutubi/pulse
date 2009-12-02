package com.zutubi.pulse.servercore.jetty;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.core.api.PulseRuntimeException;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.pulse.servercore.bootstrap.SystemConfiguration;
import com.zutubi.util.Constants;
import com.zutubi.util.logging.Logger;
import org.mortbay.http.SocketListener;
import org.mortbay.http.SslListener;
import org.mortbay.jetty.Server;
import org.springframework.beans.factory.FactoryBean;

/**
 * Used to create the Jetty server instance for Spring.
 */
public class JettyServerFactoryBean implements FactoryBean
{
    private static final Logger LOG = Logger.getLogger(JettyServerFactoryBean.class);

    private static final String PROPERTY_IDLE_TIMEOUT = "pulse.jetty.idle.timeout";
    private static final int DEFAULT_IDLE_TIMEOUT = (int) (60 * Constants.SECOND);
    private static final String PROPERTY_MIN_THREADS = "pulse.jetty.min.threads";
    private static final String PROPERTY_MAX_THREADS = "pulse.jetty.max.threads";

    private Server instance;

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
                    SystemConfiguration systemConfiguration = configManager.getSystemConfig();
                    SocketListener listener;
                    if (systemConfiguration.isSslEnabled())
                    {
                        listener = createSSLListener(systemConfiguration);
                    }
                    else
                    {
                        listener = new SocketListener();
                    }

                    listener.setHost(systemConfiguration.getBindAddress());
                    listener.setPort(systemConfiguration.getServerPort());
                    listener.setMaxIdleTimeMs(getIdleTimeout());
                    listener.setMinThreads(Integer.getInteger(PROPERTY_MIN_THREADS, listener.getMinThreads()));
                    listener.setMaxThreads(Integer.getInteger(PROPERTY_MAX_THREADS, listener.getMaxThreads()));
                    instance.addListener(listener);
                }
            }
        }
        return instance;
    }

    private SocketListener createSSLListener(SystemConfiguration systemConfiguration)
    {
        SslListener sslListener = new SslListener();
        if (TextUtils.stringSet(systemConfiguration.getSslKeystore()))
        {
            sslListener.setKeystore(systemConfiguration.getSslKeystore());
        }

        if (TextUtils.stringSet(systemConfiguration.getSslPassword()))
        {
            sslListener.setPassword(systemConfiguration.getSslPassword());
        }
        else
        {
            throw new PulseRuntimeException("SSL enabled but ssl.password not set");
        }

        if (TextUtils.stringSet(systemConfiguration.getSslKeyPassword()))
        {
            sslListener.setKeyPassword(systemConfiguration.getSslKeyPassword());
        }
        else
        {
            throw new PulseRuntimeException("SSL enabled but ssl.keyPassword not set");
        }

        return sslListener;
    }

    private int getIdleTimeout()
    {
        String timeoutValue = System.getProperty(PROPERTY_IDLE_TIMEOUT);
        if (TextUtils.stringSet(timeoutValue))
        {
            try
            {
                return (int) (Integer.parseInt(timeoutValue) * Constants.SECOND);
            }
            catch (NumberFormatException e)
            {
                return DEFAULT_IDLE_TIMEOUT;
            }
        }
        else
        {
            return DEFAULT_IDLE_TIMEOUT;
        }
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

