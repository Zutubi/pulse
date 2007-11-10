package com.zutubi.pulse.prototype.config.user.contacts;

import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Classification;
import com.zutubi.pulse.jabber.JabberManager;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.NotificationException;
import com.zutubi.util.logging.Logger;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPConnection;

/**
 *
 */
@SymbolicName("zutubi.jabberContactConfig")
@Form(fieldOrder = {"name", "username"})
@Classification(single = "jabber")
public class JabberContactConfiguration extends ContactConfiguration
{
    private static final Logger LOG = Logger.getLogger(JabberContactConfiguration.class);
    private static final String NO_SERVER_ERROR = "Unable to send Jabber notification: Jabber server not configured.";
    private static final String NO_CONNECTION_ERROR = "Unable to send Jabber notification: not connected to Jabber server.";

    private String username;

    private JabberManager jabberManager;

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getUid()
    {
        return getUsername();
    }

    protected void internalNotify(BuildResult buildResult, String subject, String content, String mimeType) throws Exception
    {
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
                chat.sendMessage(content);
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

    public void setJabberManager(JabberManager jabberManager)
    {
        this.jabberManager = jabberManager;
    }
}
