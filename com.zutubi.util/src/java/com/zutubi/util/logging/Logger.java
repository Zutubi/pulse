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

package com.zutubi.util.logging;

import java.util.logging.Filter;
import java.util.logging.Level;

/**
 * Simple wrapper around the java.util.logging.Logger to provide some utility methods.
 *
 */
public class Logger
{
    private java.util.logging.Logger delegate;

    private String sourceClass;
    private String sourceMethod;

    protected Logger(java.util.logging.Logger delegate)
    {
        this.delegate = delegate;
    }

    public static Logger getLogger(Class cls)
    {
        return Logger.getLogger(cls.getName());
    }

    public static Logger getLogger(String name)
    {
        return new Logger(java.util.logging.Logger.getLogger(name));
    }

    public void setUseParentHandlers(boolean b)
    {
        delegate.setUseParentHandlers(b);
    }

    public void severe(Throwable t)
    {
        severe(t.getMessage(), t);
    }

    public void severe(String msg)
    {
        severe(msg, (Throwable) null);
    }

    public void severe(String msg, Object... args)
    {
        severe(String.format(msg, args), (Throwable) null);
    }

    public void severe(String msg, Throwable t)
    {
        if (!isLoggable(Level.SEVERE))
        {
            return;
        }
        inferCaller();
        delegate.logp(Level.SEVERE, sourceClass, sourceMethod, msg, t);
    }

    public void info(Throwable t)
    {
        info(t.getMessage(), t);
    }

    public void info(String msg)
    {
        info(msg, null);
    }

    public void info(String msg, Throwable t)
    {
        if (!isLoggable(Level.INFO))
        {
            return;
        }
        inferCaller();
        delegate.logp(Level.INFO, sourceClass, sourceMethod, msg, t);
    }

    public void warning(Throwable t)
    {
        warning(t.getMessage(), t);
    }

    public void warning(String msg)
    {
        warning(msg, (Throwable)null);
    }

    public void warning(String msg, Object... args)
    {
        warning(String.format(msg, args), (Throwable)null);
    }

    public void warning(String msg, Throwable t)
    {
        if (!isLoggable(Level.WARNING))
        {
            return;
        }
        inferCaller();
        delegate.logp(Level.WARNING, sourceClass, sourceMethod, msg, t);
    }

    public void fine(Throwable t)
    {
        fine(t.getMessage(), t);
    }

    public void fine(String msg)
    {
        fine(msg, null);
    }

    public void fine(String msg, Throwable t)
    {
        if (!isLoggable(Level.FINE))
        {
            return;
        }
        inferCaller();
        delegate.logp(Level.FINE, sourceClass, sourceMethod, msg, t);
    }

    public void finer(Throwable t)
    {
        finer(t.getMessage(), t);
    }

    public void finer(String msg)
    {
        finer(msg, null);
    }

    public void finer(String msg, Throwable t)
    {
        if (!isLoggable(Level.FINER))
        {
            return;
        }
        inferCaller();
        delegate.logp(Level.FINER, sourceClass, sourceMethod, msg, t);
    }

    public void finest(String msg)
    {
        finest(msg, null);
    }

    public void finest(Throwable t)
    {
        finest(t.getMessage(), t);
    }

    public void finest(String msg, Throwable t)
    {
        if (!isLoggable(Level.FINEST))
        {
            return;
        }
        inferCaller();
        delegate.logp(Level.FINEST, sourceClass, sourceMethod, msg, t);
    }

    public void log(Level level, String msg)
    {
        if (!isLoggable(level))
        {
            return;
        }
        inferCaller();
        delegate.log(level, msg);
    }

    public void log(Level level, String msg, Throwable t)
    {
        if (!isLoggable(level))
        {
            return;
        }
        inferCaller();
        delegate.log(level, msg, t);
    }

    public void debug(String msg)
    {
        finest(msg);
    }

    public void debug(Throwable t)
    {
        finest(t);
    }

    public void debug(String msg, Throwable t)
    {
        finest(msg, t);
    }

    /**
     * Alias for severe.
     *
     * @param msg
     */
    public void error(String msg)
    {
        severe(msg);
    }

    public void error(Throwable t)
    {
        severe(t);
    }

    public void error(String msg, Throwable t)
    {
        severe(msg, t);
    }

    public boolean isLoggable(Level level)
    {
        return delegate.isLoggable(level);
    }

    public void entering()
    {
        if (!isLoggable(Level.FINER))
        {
            return;
        }
        inferCaller();
        entering(sourceClass, sourceMethod);
    }

    public void entering(String source, String method)
    {
        delegate.entering(source, method);
    }

    public void exiting()
    {
        if (!isLoggable(Level.FINER))
        {
            return;
        }
        inferCaller();
        delegate.exiting(sourceClass, sourceMethod);
    }

    public void exiting(Object result)
    {
        if (!isLoggable(Level.FINER))
        {
            return;
        }
        inferCaller();
        delegate.exiting(sourceClass, sourceMethod, result);
    }

    /**
     * This inferCaller method is taken from the LogRecord object. We need to reproduce it here
     * since by wrapping the Logger we break this method.
     *
     */
    private void inferCaller()
    {
        sourceClass = null;
        sourceMethod = null;

        // Get the stack trace.
        StackTraceElement stack[] = (new Throwable()).getStackTrace();
        // First, search back to a method in the Logger class.
        String myClassName = getClass().getName();
        int ix = 0;
        while (ix < stack.length)
        {
            StackTraceElement frame = stack[ix];
            String cname = frame.getClassName();
            if (cname.equals(myClassName))
            {
                break;
            }
            ix++;
        }

        // Now search for the first frame before the "Logger" class.
        while (ix < stack.length)
        {
            StackTraceElement frame = stack[ix];
            String cname = frame.getClassName();
            if (!cname.equals(myClassName))
            {
                // We've found the relevant frame.
                sourceClass = cname;
                sourceMethod = frame.getMethodName();
                return;
            }
            ix++;
        }
        // We haven't found a suitable frame, so just punt.  This is
        // OK as we are only committed to making a "best effort" here.
    }

    /**
     * Sets the log level on the delegate.
     *
     * @param level the new log level
     *
     * @see java.util.logging.Logger#setLevel(java.util.logging.Level)
     */
    public void setLevel(Level level)
    {
        delegate.setLevel(level);
    }

    public void setFilter(Filter filter)
    {
        delegate.setFilter(filter);
    }
}
