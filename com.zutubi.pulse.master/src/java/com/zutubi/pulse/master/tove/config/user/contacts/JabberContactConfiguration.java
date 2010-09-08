package com.zutubi.pulse.master.tove.config.user.contacts;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.NotificationException;
import com.zutubi.pulse.master.notifications.jabber.JabberManager;
import com.zutubi.tove.annotations.*;
import com.zutubi.util.logging.Logger;
import com.zutubi.validation.annotations.Required;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPConnection;

/**
 *
 */
@SymbolicName("zutubi.jabberContactConfig")
@Form(fieldOrder = {"name", "username"})
@Classification(single = "jabber")
@ConfigurationCheck("JabberContactConfigurationCheckHandler")
@Wire
public class JabberContactConfiguration extends ContactConfiguration
{
    private static final Logger LOG = Logger.getLogger(JabberContactConfiguration.class);
    private static final String NO_SERVER_ERROR = "Unable to send Jabber notification: Jabber server not configured.";
    private static final String NO_CONNECTION_ERROR = "Unable to send Jabber notification: not connected to Jabber server.";

    @Required
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

    public void notify(BuildResult buildResult, String subject, String content, String mimeType) throws Exception
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
