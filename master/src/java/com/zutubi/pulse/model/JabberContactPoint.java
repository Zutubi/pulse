package com.zutubi.pulse.model;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.Chat;
import com.zutubi.pulse.jabber.JabberManager;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.util.logging.Logger;
import com.zutubi.validation.annotations.Required;

/**
 * A contact point used to notify users via an XMPP IM.
 */
public class JabberContactPoint extends ContactPoint
{
    private static final Logger LOG = Logger.getLogger(JabberContactPoint.class);
    private static final String NO_SERVER_ERROR = "Unable to send Jabber notification: Jabber server not configured.";

    private JabberManager jabberManager;

    public void internalNotify(BuildResult result) throws Exception
    {
        // TODO: oh dear, again
        ComponentContext.autowire(this);

        try
        {
            XMPPConnection connection = jabberManager.getConnection();
            if(connection != null)
            {
                Chat chat = connection.createChat(getUsername());
                chat.sendMessage("project " + result.getProject().getName() + ": build " + result.getNumber() + ": " + result.getState().getPrettyString());
            }
            else
            {
                LOG.warning(NO_SERVER_ERROR);
                throw new NotificationException(NO_SERVER_ERROR);
            }
        }
        catch (Exception e)
        {
            LOG.warning("Unable to send jabber notification: " + e.getMessage(), e);
            throw e;
        }
    }

    @Required public String getUsername()
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
