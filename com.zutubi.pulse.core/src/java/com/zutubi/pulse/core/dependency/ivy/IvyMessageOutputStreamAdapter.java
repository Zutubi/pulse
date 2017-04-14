/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.core.dependency.ivy;

import com.google.common.io.ByteStreams;
import com.zutubi.util.SystemUtils;
import com.zutubi.util.logging.Logger;
import org.apache.ivy.util.Message;

import java.io.IOException;
import java.io.OutputStream;

import static com.zutubi.pulse.core.dependency.ivy.IvyUtils.PROGRESS_CHARACTER;

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
        this.output = output == null ? ByteStreams.nullOutputStream() : output;
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
                if (!msg.endsWith(SystemUtils.LINE_SEPARATOR))
                {
                    msg = msg + SystemUtils.LINE_SEPARATOR;
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
