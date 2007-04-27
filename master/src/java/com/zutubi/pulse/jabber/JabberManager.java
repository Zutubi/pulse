package com.zutubi.pulse.jabber;

import com.opensymphony.util.TextUtils;
import com.zutubi.prototype.config.ConfigurationEventListener;
import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.prototype.config.events.ConfigurationEvent;
import com.zutubi.prototype.config.events.PostSaveEvent;
import com.zutubi.pulse.core.Stoppable;
import com.zutubi.pulse.prototype.config.admin.JabberConfiguration;
import com.zutubi.util.Constants;
import com.zutubi.util.logging.Logger;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.XMPPError;

/**
 */
public class JabberManager implements Stoppable, PacketListener, ConfigurationEventListener
{
    public static final int DEFAULT_PORT = 5222;

    private static final Logger LOG = Logger.getLogger(JabberManager.class);

    private XMPPConnection connection = null;
    private ConfigurationProvider configurationProvider;

    /**
     * Holds the status from when we last tried to connect.  Will be null
     * if everything was ok.
     */
    private String statusMessage = null;
    private long lastFailureTime = -1;

    public synchronized void init()
    {
        configurationProvider.registerEventListener(this, false, false, JabberConfiguration.class);
        JabberConfiguration config = configurationProvider.get(JabberConfiguration.class);
        init(config);
    }

    private void init(JabberConfiguration config)
    {
        statusMessage = null;
        if (config != null && TextUtils.stringSet(config.getServer()))
        {
            LOG.info("Initialising Jabber");
            Roster.setDefaultSubscriptionMode(Roster.SUBSCRIPTION_ACCEPT_ALL);
            try
            {
                openConnection(config);
            }
            catch (Exception e)
            {
                // Workaround for SSL negotiation problems: try once more!
                LOG.warning("Retrying Jabber initialisation");
                try
                {
                    stop(true);
                    openConnection(config);
                }
                catch(Exception nesty)
                {
                    connectionError("Could not initialise Jabber: " + nesty.getMessage());
                }
            }
        }

        if(connection != null)
        {
            LOG.info("Jabber initialised");
        }
    }

    public boolean isConfigured()
    {
        JabberConfiguration config = configurationProvider.get(JabberConfiguration.class);
        return config != null && TextUtils.stringSet(config.getServer());
    }

    private void openConnection(JabberConfiguration config) throws XMPPException
    {
        connection = getConnection(config);
        lastFailureTime = -1;
        connection.addPacketListener(this, new MessageTypeFilter(Message.Type.ERROR));
    }

    private XMPPConnection getConnection(JabberConfiguration config) throws XMPPException
    {
        XMPPConnection connection;

        if (config.getServer().endsWith("google.com"))
        {
            connection = new GoogleTalkConnection();
        }
        else if(config.isSsl())
        {
            connection = new SSLXMPPConnection(config.getServer(), config.getPort());
        }
        else
        {
            connection = new XMPPConnection(config.getServer(), config.getPort());
        }

        connection.login(config.getUsername(), config.getPassword());
        return connection;
    }

    public void testConnection(JabberConfiguration config) throws XMPPException
    {
        XMPPConnection connection = null;

        try
        {
            try
            {
                connection = getConnection(config);
            }
            catch (XMPPException e)
            {
                // Second try to workaround SSL negotiation issues
                connection = getConnection(config);
            }
        }
        finally
        {
            if(connection != null)
            {
                connection.close();
            }
        }
    }

    public XMPPConnection getConnection()
    {
        // Try automagic reconnection at most a minute after the last error
        // to prevent hammering away at a broken connection.
        if(connection == null && (lastFailureTime < 0 || lastFailureTime + Constants.MINUTE < System.currentTimeMillis()))
        {
            init();
        }

        return connection;
    }

    /**
     * Used to report an error when attempting to establish or use the Jabber
     * connection.
     *
     * @param message a description of the error
     */
    public void connectionError(String message)
    {
        stop(true);
        statusMessage = message;
        lastFailureTime = System.currentTimeMillis();
        LOG.warning(statusMessage);
    }

    public synchronized void stop(boolean force)
    {
        if(connection != null)
        {
            try
            {
                connection.close();
            }
            catch (Exception e)
            {
                // Ignore
            }
            connection = null;
        }
    }

    public void processPacket(Packet packet)
    {
        XMPPError error = packet.getError();
        if(error != null)
        {
            String message = "Jabber error";

            if(packet.getFrom()  != null)
            {
                message += ": from " + packet.getFrom();
            }

            message += ": code " + error.getCode();

            if(error.getMessage()  != null)
            {
                message += ": " + error.getMessage();
            }

            LOG.error(message);
        }
    }

    public String getStatusMessage()
    {
        return statusMessage;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }

    public void handleConfigurationEvent(ConfigurationEvent event)
    {
        if(event instanceof PostSaveEvent)
        {
            stop(true);
            init((JabberConfiguration) ((PostSaveEvent)event).getNewInstance());
        }
    }
}
