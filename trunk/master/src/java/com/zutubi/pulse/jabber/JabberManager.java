package com.zutubi.pulse.jabber;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.bootstrap.MasterConfiguration;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.Stoppable;
import com.zutubi.pulse.util.logging.Logger;
import com.zutubi.pulse.util.Constants;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.XMPPError;

/**
 */
public class JabberManager implements Stoppable, PacketListener
{
    public static final int DEFAULT_PORT = 5222;

    private static final Logger LOG = Logger.getLogger(JabberManager.class);

    private XMPPConnection connection = null;
    private MasterConfigurationManager configurationManager;

    /**
     * Holds the status from when we last tried to connect.  Will be null
     * if everything was ok.
     */
    private String statusMessage = null;
    private long lastFailureTime = -1;

    public synchronized void init()
    {
        statusMessage = null;
        MasterConfiguration appConfig = configurationManager.getAppConfig();
        if (isConfigured())
        {
            LOG.info("Initialising Jabber");
            Roster.setDefaultSubscriptionMode(Roster.SUBSCRIPTION_ACCEPT_ALL);
            try
            {
                openConnection(appConfig);
            }
            catch (Exception e)
            {
                // Workaround for SSL negotiation problems: try once more!
                LOG.warning("Retrying Jabber initialisation");
                try
                {
                    stop(true);
                    openConnection(appConfig);
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
        return TextUtils.stringSet(configurationManager.getAppConfig().getJabberHost());
    }

    private void openConnection(MasterConfiguration appConfig) throws XMPPException
    {
        connection = openConnection(appConfig.getJabberHost(), appConfig.getJabberPort(), appConfig.getJabberUsername(), appConfig.getJabberPassword(), appConfig.getJabberForceSSL());
        connection.addPacketListener(this, new MessageTypeFilter(Message.Type.ERROR));
    }

    private XMPPConnection openConnection(String host, int port, String username, String password, boolean forceSSL) throws XMPPException
    {
        XMPPConnection connection = null;

        if (host.endsWith("google.com"))
        {
            connection = new GoogleTalkConnection();
        }
        else if(forceSSL)
        {
            connection = new SSLXMPPConnection(host, port);
        }
        else
        {
            connection = new XMPPConnection(host, port);
        }

        connection.login(username, password);
        return connection;
    }

    public void testConnection(String host, int port, String username, String password, boolean forceSSL) throws XMPPException
    {
        XMPPConnection connection = null;

        try
        {
            try
            {
                connection = openConnection(host, port, username, password, forceSSL);
            }
            catch (XMPPException e)
            {
                // Second try to workaround SSL negotiation issues
                connection = openConnection(host, port, username, password, forceSSL);
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

    public void refresh()
    {
        stop(true);
        init();
    }

    public String getStatusMessage()
    {
        return statusMessage;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
