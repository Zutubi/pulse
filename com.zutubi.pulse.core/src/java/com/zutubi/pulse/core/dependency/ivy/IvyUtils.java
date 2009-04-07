package com.zutubi.pulse.core.dependency.ivy;

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
        // Implementation note: this encoding is deliberately verbose to make it clearer what
        // has been done when looking at the repository contents.  There is no practical reason
        // to make this encoding obscure.

        // Implementation note 2: this encoding gets around a number of issues.  Firstly, ivy config
        // patterns do not support an escape character, so characters with special meaning need to be
        // changed.  Secondly, the stage name is used as part of a URL.  If we were to use standard
        // percentage encoding (as is used for special characters in URLs), then the repository will
        // decode these and fail to write a file to the windows file system if it contains the '*'.
        // The repository also interprets the ';' in the URL as an alias indicator, causing further
        // problems.
        // So, whilst the current encoding is not completely fool proof, it is pragmattic enough to
        // work in the majority of cases and be clear enough to allow quick understanding of what is
        // going on.

        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < stageName.length(); i++)
        {
            char c = stageName.charAt(i);
            switch (c)
            {
                case ',':
                    buffer.append("_comma_");
                    break;
                case ';':
                    buffer.append("_semicolon_");
                    break;
                case '%':
                    buffer.append("_percent_");
                    break;
                case '#':
                    buffer.append("_hash_");
                    break;
                case '*':
                    buffer.append("_star_");
                    break;
                case '=':
                    buffer.append("_equals_");
                    break;
                default:
                    buffer.append(c);
            }
        }
        return buffer.toString();
    }
}
