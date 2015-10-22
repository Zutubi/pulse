package com.zutubi.pulse.master.tove.config.user.contacts;

import com.zutubi.pulse.master.model.NotificationException;
import com.zutubi.pulse.master.notifications.NotificationAttachment;
import com.zutubi.pulse.master.notifications.jabber.JabberManager;
import com.zutubi.pulse.master.notifications.renderer.RenderedResult;
import com.zutubi.tove.annotations.Classification;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wire;
import com.zutubi.util.logging.Logger;
import com.zutubi.validation.annotations.Required;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPConnection;

import java.util.List;

/**
 * Represents a Jabber account that can receive isntant message style notifications.
 */
@SymbolicName("zutubi.jabberContactConfig")
@Form(fieldOrder = {"name", "username"})
@Classification(single = "jabber")
@Wire
public class JabberContactConfiguration extends ContactConfiguration
{
    private static final Logger LOG = Logger.getLogger(JabberContactConfiguration.class);
    private static final String NO_SERVER_ERROR = "Unable to send Jabber notification: Jabber server not configured.";
    private static final String NO_CONNECTION_ERROR = "Unable to send Jabber notification: not connected to Jabber server.";
    
    private static final String PROPERTY_MESSAGE_LENGTH_LIMIT = "pulse.jabber.length.limit";
    private static final int DEFAULT_MESSAGE_LENGTH_LIMIT = 16384;
    private static final int MESSAGE_LENGTH_LIMIT = Integer.getInteger(PROPERTY_MESSAGE_LENGTH_LIMIT, DEFAULT_MESSAGE_LENGTH_LIMIT);

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

    public String getUniqueId()
    {
        return getUsername();
    }

    @Override
    public boolean supportsAttachments()
    {
        return false;
    }

    public void notify(RenderedResult rendered, List<NotificationAttachment> attachments) throws Exception
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
                String message = rendered.getContent();
                if (message.length() > MESSAGE_LENGTH_LIMIT)
                {
                    LOG.warning("Jabber message too long (" + message.length() + " bytes), trimming to " + MESSAGE_LENGTH_LIMIT + " bytes (set system property '" + PROPERTY_MESSAGE_LENGTH_LIMIT + "' to change this limit).");
                    message = rendered.getContentTrimmed(MESSAGE_LENGTH_LIMIT);
                }
                chat.sendMessage(message);
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
