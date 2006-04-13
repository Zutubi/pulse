/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.scm.cvs.client;

import org.netbeans.lib.cvsclient.event.BinaryMessageEvent;
import org.netbeans.lib.cvsclient.event.CVSAdapter;
import org.netbeans.lib.cvsclient.event.EnhancedMessageEvent;
import org.netbeans.lib.cvsclient.event.MessageEvent;

import com.zutubi.pulse.util.logging.Logger;

/**
 *
 *
 */
public class LoggingListener extends CVSAdapter
{
    private final StringBuffer taggedLine = new StringBuffer();

    private static final Logger LOG = Logger.getLogger(LoggingListener.class);

    /**
     * Called when the server wants to send a message to be displayed to
     * the user. The message is only for information purposes and clients
     * can choose to ignore these messages if they wish.
     *
     * @param e the event
     */
    public void messageSent(MessageEvent e)
    {
        String line = e.getMessage();
        if (e instanceof EnhancedMessageEvent)
        {
            return;
        }

        if (e.isTagged())
        {
            String message = MessageEvent.parseTaggedMessage(taggedLine, e.getMessage());
            if (message != null)
            {
                if (e.isError())
                {
                    LOG.warning(message);
                }
                else
                {
                    LOG.finest(message);
                }
            }
        }
        else
        {
            LOG.finest(line);
        }
    }

    /**
     * Called when the server wants to send a binary message to be displayed to
     * the user. The message is only for information purposes and clients
     * can choose to ignore these messages if they wish.
     *
     * @param e the event
     */
    public void messageSent(BinaryMessageEvent e)
    {
        byte[] bytes = e.getMessage();
        LOG.finest(new String(bytes));
    }
}
