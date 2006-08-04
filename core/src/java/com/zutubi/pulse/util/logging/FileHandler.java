package com.zutubi.pulse.util.logging;

import com.zutubi.pulse.command.PulseCtl;

import java.io.IOException;

/**
 * Filehandler implementation that provides additional patterns.
 * <ul>
 * <li> %b   the pulse.home system property.</li>
 * </ul>
 */
public class FileHandler extends java.util.logging.FileHandler
{
    public FileHandler() throws IOException
    {
        super();
    }

    public FileHandler(String pattern) throws IOException
    {
        super(doSubstitution(pattern));
    }

    public FileHandler(String pattern, boolean append) throws IOException
    {
        super(doSubstitution(pattern), append);
    }

    public FileHandler(String pattern, int limit, int count) throws IOException
    {
        super(doSubstitution(pattern), limit, count);
    }

    public FileHandler(String pattern, int limit, int count, boolean append) throws IOException
    {
        super(doSubstitution(pattern), limit, count, append);
    }

    private static String doSubstitution(String pattern)
    {
        if (pattern == null)
        {
            return pattern;
        }

        // only handle substitution if pulse.home is set.
        if (!System.getProperties().contains(PulseCtl.PULSE_HOME))
        {
            return pattern;
        }

        // WARNING: The following implementation does not support the escaping of the '%' character.
        String pulseHome = System.getProperty(PulseCtl.PULSE_HOME);

        int index = pattern.indexOf("%b");
        while (index != -1)
        {
            pattern = pattern.substring(0, index) + pulseHome + pattern.substring(index + 2);
            index = pattern.indexOf("%b");
        }
        return pattern;
    }
}
