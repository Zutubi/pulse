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

package com.zutubi.pulse.servercore.util.logging;

import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.pulse.servercore.bootstrap.SystemPaths;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.*;

/**
 * Filehandler implementation that provides additional patterns.
 * <ul>
 * <li> %l   the pulse logging directory.</li>
 * </ul>
 */
public class FileHandler extends Handler
{
    private java.util.logging.FileHandler delegate;

    private String pattern;
    private int limit;
    private int count;
    private boolean append;

    private SystemPaths paths;

    public FileHandler()
    {
    }

    public void publish(LogRecord record)
    {
        getDelegate().publish(record);
    }

    public void flush()
    {
        if (delegate != null)
        {
            delegate.flush();
        }
    }

    public void close() throws SecurityException
    {
        if (delegate != null)
        {
            delegate.close();
        }
    }

    protected void reportError(String msg, Exception ex, int code)
    {
        delegate.getErrorManager().error(msg, ex, code);
    }

    public void setPattern(String pattern)
    {
        this.pattern = pattern;
    }

    public void setLimit(int limit)
    {
        this.limit = limit;
    }

    public void setCount(int count)
    {
        this.count = count;
    }

    public void setAppend(boolean append)
    {
        this.append = append;
    }

    public void setLevel(Level l)
    {
        super.setLevel(l);
        if (delegate != null)
        {
            delegate.setLevel(l);
        }
    }

    public void setFormatter(Formatter f)
    {
        super.setFormatter(f);
        if (delegate != null)
        {
            delegate.setFormatter(f);
        }
    }

    public void setFilter(Filter f)
    {
        super.setFilter(f);
        if (delegate != null)
        {
            delegate.setFilter(f);
        }
    }

    public void setEncoding(String encoding) throws UnsupportedEncodingException
    {
        super.setEncoding(encoding);
        if (delegate != null)
        {
            delegate.setEncoding(encoding);
        }
    }

    public void setErrorManager(ErrorManager em)
    {
        super.setErrorManager(em);
        if (delegate != null)
        {
            delegate.setErrorManager(em);
        }
    }

    private synchronized java.util.logging.FileHandler getDelegate()
    {
        if (delegate == null)
        {
            try
            {
                delegate = new java.util.logging.FileHandler(doSubstitution(pattern), limit, count, append);
                delegate.setLevel(getLevel());
                delegate.setFormatter(getFormatter());
                delegate.setFilter(getFilter());
                delegate.setEncoding(getEncoding());
                delegate.setErrorManager(getErrorManager());
            }
            catch (IOException e)
            {
                System.err.println("Failed to initialise file logger. Cause: " + e.getMessage());
                e.printStackTrace(System.err);

                // disable? or continuously print exceptions each time someone logs a message...
            }
        }
        return delegate;
    }

    public void setSystemPaths(SystemPaths paths)
    {
        this.paths = paths;
    }

    public void setConfigurationManager(ConfigurationManager configManager)
    {
        setSystemPaths(configManager.getSystemPaths());
    }

    private String doSubstitution(String pattern)
    {
        if (pattern == null)
        {
            return pattern;
        }

        // we need to ensure that the path exist
        File logRoot = paths.getLogRoot();

        StringBuffer buffer = new StringBuffer();

        int ix = 0;
        while (ix < pattern.length()) {
            char ch = pattern.charAt(ix);
            char ch2 = 0;
            ix++;

            // lookahead.
            if (ix < pattern.length())
            {
                ch2 = pattern.charAt(ix);
            }

            if (ch == '%')
            {
                if (ch2 == '%')
                {
                    buffer.append(ch2);
                    ix++;
                }
                else if (ch2 == 'l')
                {
                    // replace with log root.
                    try
                    {
                        buffer.append(logRoot.getCanonicalPath());
                    }
                    catch (IOException e)
                    {
                        buffer.append(logRoot.getAbsolutePath());
                    }
                    ix++;
                }
                else
                {
                    // let it pass through and on to the file handlers own substitution.
                    buffer.append(ch);
                    buffer.append(ch2);
                    ix++;
                }
            }
            else
            {
                buffer.append(ch);
            }
        }

        return buffer.toString();
    }
}
