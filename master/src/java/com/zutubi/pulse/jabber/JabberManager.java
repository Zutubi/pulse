package com.zutubi.pulse.jabber;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.bootstrap.MasterConfiguration;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.Stoppable;
import com.zutubi.pulse.util.logging.Logger;
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

    public void init()
    {
        statusMessage = null;
        MasterConfiguration appConfig = configurationManager.getAppConfig();
        if (TextUtils.stringSet(appConfig.getJabberHost()))
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
                    stop(true);
                    statusMessage = "Could not initialise Jabber: " + nesty.getMessage();
                    LOG.warning(statusMessage);
                }
            }
        }

        if(connection != null)
        {
            LOG.info("Jabber initialised");
        }
    }

    private void openConnection(MasterConfiguration appConfig)
            throws XMPPException
    {
        connection = openConnection(appConfig.getJabberHost(), appConfig.getJabberPort(), appConfig.getJabberUsername(), appConfig.getJabberPassword(), appConfig.getJabberForceSSL());
        connection.addPacketListener(this, new MessageTypeFilter(Message.Type.ERROR));
    }

    private XMPPConnection openConnection(String host, int port, String username, String password, boolean forceSSL)
            throws XMPPException
    {
        XMPPConnection connection = null;

        if(forceSSL)
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
        return connection;
    }

    public void stop(boolean force)
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
