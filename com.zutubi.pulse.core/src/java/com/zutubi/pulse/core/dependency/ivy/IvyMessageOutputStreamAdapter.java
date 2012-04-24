package com.zutubi.pulse.core.dependency.ivy;

import static com.zutubi.pulse.core.dependency.ivy.IvyUtils.PROGRESS_CHARACTER;
import com.zutubi.util.Constants;
import com.zutubi.util.io.NullOutputStream;
import com.zutubi.util.logging.Logger;
import org.apache.ivy.util.Message;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An implementation of the ivy message logger base class that redirects some of the log
 * messages to an output stream.
 */
public class IvyMessageOutputStreamAdapter extends org.apache.ivy.util.AbstractMessageLogger
{
    private static final Logger LOG = Logger.getLogger(IvyMessageOutputStreamAdapter.class);

    /**
     * The output stream to which selected messages will be sent.
     */
    private OutputStream output;

    public IvyMessageOutputStreamAdapter(OutputStream output)
    {
        this.output = output == null ? new NullOutputStream() : output;
    }

    protected void doProgress()
    {
        try
        {
            output.write(PROGRESS_CHARACTER);
        }
        catch (IOException e)
        {
            LOG.error(e);
        }
    }

    protected void doEndProgress(String msg)
    {
        try
        {
            output.write(msg.getBytes());
        }
        catch (IOException e)
        {
            LOG.error(e);
        }
    }

    public void rawlog(String msg, int level)
    {
        log(msg, level);
    }

    public void log(String msg, int level)
    {
        try
        {
            if (level <= Message.MSG_INFO)
            {
                // some of the messages from ivy have new line characters at the end, others do not.  So
                // that we get something sensible in our logs, we apply a new line when necessary.
                if (!msg.endsWith(Constants.LINE_SEPARATOR))
                {
                    msg = msg + Constants.LINE_SEPARATOR;
                }
                output.write(msg.getBytes());
            }
        }
        catch (IOException e)
        {
            LOG.error(e);
        }
    }
}
