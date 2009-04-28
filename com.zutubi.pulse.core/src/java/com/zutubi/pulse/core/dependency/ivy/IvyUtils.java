package com.zutubi.pulse.core.dependency.ivy;

import com.zutubi.util.StringUtils;
import org.apache.ivy.util.Message;

import java.util.logging.Level;

/**
 * A set of utility methods to help with adapting the ivy logging system to
 * Pulse.
 */
public class IvyUtils
{
    /**
     * The character used to indicate that an ivy action has made some sort of progress.
     */
    public static final byte[] PROGRESS_CHARACTER = ".".getBytes();

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

    /**
     * The dependency management process based on ivy has some special requirements with
     * regards to the characters used for stage names.
     *
     * The Pulse stage names are used for both ivy config names / patterns and for the
     * url path at which there artifacts are stored.  To ensure that there are no problems
     * with any of these processes, all pulse stage names (which are unrestricted in what
     * characters they contain) must be ivyEncoded before being used.
     *
     * @param stageName the string to be encoded.
     * @return the encoded string.
     */
    public static String ivyEncodeStageName(String stageName)
    {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < stageName.length(); i++)
        {
            char character = stageName.charAt(i);
            if(StringUtils.isAsciiAlphaNumeric(character))
            {
                buffer.append(character);
            }
            else
            {
                // A few more likely-used characters
                switch(character)
                {
                    case ' ':
                    case '-':
                    case '.':
                        buffer.append(character);
                        break;
                    default:
                        buffer.append("_").append((int)character);
                        break;
                }

            }
        }
        return buffer.toString();
    }
}
