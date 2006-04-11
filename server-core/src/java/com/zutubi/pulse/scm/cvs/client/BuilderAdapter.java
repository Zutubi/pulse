package com.zutubi.pulse.scm.cvs.client;

import org.netbeans.lib.cvsclient.command.Builder;
import org.netbeans.lib.cvsclient.event.CVSAdapter;
import org.netbeans.lib.cvsclient.event.EnhancedMessageEvent;
import org.netbeans.lib.cvsclient.event.MessageEvent;

/**
 * It would be nice if the HistoryCommand were buildable, but its not, so 
 * this adapter is the next best thing.  
 *
 */
public class BuilderAdapter extends CVSAdapter
{
    private StringBuffer taggedLineBuffer = new StringBuffer();
    
    private final Builder builder;
    
    public BuilderAdapter(Builder builder)
    {
        this.builder = builder;
    }
    
    public void messageSent(MessageEvent e)
    {
        if (builder == null)
        {
            return;
        }

        if (e instanceof EnhancedMessageEvent)
        {
            EnhancedMessageEvent eEvent = (EnhancedMessageEvent) e;
            builder.parseEnhancedMessage(eEvent.getKey(), eEvent.getValue());
            return;
        }

        if (e.isTagged())
        {
            String message = MessageEvent.parseTaggedMessage(taggedLineBuffer, e.getMessage());
            if (message != null)
            {
                builder.parseLine(message, false);
                taggedLineBuffer.setLength(0);
            }
        }
        else
        {
            if (taggedLineBuffer.length() > 0)
            {
                builder.parseLine(taggedLineBuffer.toString(), false);
                taggedLineBuffer.setLength(0);
            }
            builder.parseLine(e.getMessage(), e.isError());
        }
    }

}
