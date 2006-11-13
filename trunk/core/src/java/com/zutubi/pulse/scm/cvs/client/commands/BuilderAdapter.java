package com.zutubi.pulse.scm.cvs.client.commands;

import org.netbeans.lib.cvsclient.command.Builder;
import org.netbeans.lib.cvsclient.event.*;

public class BuilderAdapter extends CVSAdapter
{

    public BuilderAdapter(Builder builder)
    {
        taggedLineBuffer = new StringBuffer();
        this.builder = builder;
    }

    public void messageSent(MessageEvent e)
    {
        if(builder == null)
            return;
        if(e instanceof EnhancedMessageEvent)
        {
            EnhancedMessageEvent eEvent = (EnhancedMessageEvent)e;
            builder.parseEnhancedMessage(eEvent.getKey(), eEvent.getValue());
            return;
        }
        if(e.isTagged())
        {
            String message = MessageEvent.parseTaggedMessage(taggedLineBuffer, e.getMessage());
            if(message != null)
            {
                builder.parseLine(message, false);
                taggedLineBuffer.setLength(0);
            }
        } else
        {
            if(taggedLineBuffer.length() > 0)
            {
                builder.parseLine(taggedLineBuffer.toString(), false);
                taggedLineBuffer.setLength(0);
            }
            builder.parseLine(e.getMessage(), e.isError());
        }
    }

    private StringBuffer taggedLineBuffer;
    private final Builder builder;
}
