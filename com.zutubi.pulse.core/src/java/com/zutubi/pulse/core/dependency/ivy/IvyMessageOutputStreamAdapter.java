package com.zutubi.pulse.core.dependency.ivy;

import static com.zutubi.pulse.core.dependency.ivy.IvyUtils.toLevel;
import static com.zutubi.pulse.core.dependency.ivy.IvyUtils.PROGRESS_CHARACTER;
import com.zutubi.util.logging.Logger;
import com.zutubi.util.Constants;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An implementation of the ivy message logger base class that redirects some of the log
 * messages to an output stream.
 *
 * The logging level of messages logged to the output stream are controlled via the usual
 * means, the logging.properties files, setting a logging level to this class.
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
        this.output = output;
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
            if (isLoggable(level))
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

    /**
     * Indicates whether or not messages at the specified logging level should
     * be logged.  If we return false, any messages at that level should be ignored.
     *
     * @param level the logging level in question.
     *
     * @return true if a message at that logging level should be logged, false if it should
     * be ignored.
     */
    private boolean isLoggable(int level)
    {
        return LOG.isLoggable(toLevel(level));
    }
}
