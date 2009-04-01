package com.zutubi.pulse.core.dependency.ivy;

import org.apache.ivy.util.Message;
import org.apache.ivy.util.MessageLogger;
import org.apache.ivy.core.settings.IvySettings;

import java.util.logging.Level;

import com.zutubi.util.NullaryFunction;

/**
 * A set of utility methods to help with adapting the ivy logging system to
 * Pulse.
 */
public class IvyLogUtils
{
    /**
     * Convert an ivy message level to a comparable java logging level.
     *
     * @param level ivy level
     * 
     * @see org.apache.ivy.util.Message#MSG_INFO
     * @see org.apache.ivy.util.Message#MSG_DEBUG
     * @see org.apache.ivy.util.Message#MSG_ERR
     * @see org.apache.ivy.util.Message#MSG_VERBOSE
     * @see org.apache.ivy.util.Message#MSG_WARN
     *
     * @return java logging level.
     */
    public static Level toLevel(int level)
    {
        switch (level)
        {
            case Message.MSG_INFO:
                return Level.INFO;
            case Message.MSG_DEBUG:
                return Level.FINEST;
            case Message.MSG_ERR:
                return Level.SEVERE;
            case Message.MSG_VERBOSE:
                return Level.FINE;
            case Message.MSG_WARN:
                return Level.WARNING;
        }
        throw new RuntimeException("Unknown logging level: " + level);
    }

    public static <T> T runQuietly(NullaryFunction<T> function)
    {
        MessageLogger defaultLogger = Message.getDefaultLogger();
        Message.setDefaultLogger(new NullMessageLogger());
        try
        {
            return function.process();
        }
        finally
        {
            Message.setDefaultLogger(defaultLogger);
        }
    }

}
