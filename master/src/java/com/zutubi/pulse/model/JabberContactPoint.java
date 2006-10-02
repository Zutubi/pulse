package com.zutubi.pulse.model;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.Chat;
import com.zutubi.pulse.jabber.JabberManager;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.util.logging.Logger;

/**
 * A contact point used to notify users via an XMPP IM.
 */
public class JabberContactPoint extends ContactPoint
{
    private static final Logger LOG = Logger.getLogger(JabberContactPoint.class);
    private static final String NO_SERVER_ERROR = "Unable to send Jabber notification: Jabber server not configured.";
    private static final String NO_CONNECTION_ERROR = "Unable to send Jabber notification: not connected to Jabber server.";

    private JabberManager jabberManager;

    public void internalNotify(BuildResult result) throws Exception
    {
        // TODO: oh dear, again
        ComponentContext.autowire(this);

        if (!jabberManager.isConfigured())
        {
            LOG.warning(NO_SERVER_ERROR);
            throw new NotificationException(NO_SERVER_ERROR);
        }

        XMPPConnection connection = jabberManager.getConnection();
        if (connection != null)
        {
            try
            {
                Chat chat = connection.createChat(getUsername());
                chat.sendMessage("project " + result.getProject().getName() + ": build " + result.getNumber() + ": " + result.getState().getPrettyString());
            }
            catch (Exception e)
            {
                String message = "Unable to send jabber notification: " + e.getMessage();
                jabberManager.connectionError(message);
                LOG.warning(message, e);
                throw e;
            }
        }
        else
        {
            LOG.warning(NO_CONNECTION_ERROR);
            throw new NotificationException(NO_CONNECTION_ERROR);
        }
    }

    public String getUsername()
    {
        return getUid();
    }

    public void setUsername(String username)
    {
        setUid(username);
    }

    public void setJabberManager(JabberManager jabberManager)
    {
        this.jabberManager = jabberManager;
    }
}
