package com.zutubi.pulse.core.scm.cvs.client.commands;

import org.netbeans.lib.cvsclient.event.CVSAdapter;
import org.netbeans.lib.cvsclient.event.MessageEvent;

import java.io.PrintWriter;

/**
 * A CVS listener that just writes all output to a PrintWriter.
 */
public class OutputListener extends CVSAdapter
{
    private PrintWriter writer;
    private final StringBuffer taggedLine = new StringBuffer();

    public OutputListener(PrintWriter writer)
    {
        this.writer = writer;
    }

    public void messageSent(MessageEvent e)
    {
        if (!e.isError())
        {
            String line = e.getMessage();
            if (e.isTagged())
            {
                String message = MessageEvent.parseTaggedMessage(taggedLine, line);
                if (message != null)
                {
                    writer.println(message);
                    taggedLine.setLength(0);
                }
            }
            else
            {
                writer.println(line);
            }
        }
    }

}
