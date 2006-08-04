package com.zutubi.pulse.logging;

import com.zutubi.pulse.command.PulseCtl;

import java.io.IOException;
import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;
import java.util.logging.Logger;

/**
 * The file logger is initialised at a time and in a context that does
 * not have access to the running system. Therefore, it makes a best guess
 * at where the logging directory is located.
 *
 * @author Daniel Ostermeier
 */
public class FileConfig
{
    private static final String PULSE_HOME = PulseCtl.PULSE_HOME;

    private static final String FILE_NAME = "pulse%u.%g.log";

    private static final int APPROX_FILESIZE_LIMIT = 1000000;
    private static final int FILE_ROLL_COUNT = 20;
    private static final boolean APPEND = true;

    public FileConfig() throws IOException
    {
        // the default configuration is pulse.home/system/logs
        if (!System.getProperties().containsKey(PULSE_HOME))
        {
            return;
        }

        String pulseHomeProperty = System.getProperty(PULSE_HOME);
        File pulseHome = new File(pulseHomeProperty);

        // log root is just below the pulse home.
        File logRoot = new File(pulseHome, asPath("system", "logs"));

        if (!logRoot.exists() && !logRoot.mkdirs())
        {
            throw new IOException();
        }
        if (logRoot.exists() && !logRoot.isDirectory())
        {
            throw new IOException();
        }

        String pattern = logRoot.getCanonicalPath() + File.separator + FILE_NAME;

        FileHandler fileHandler = new FileHandler(pattern, APPROX_FILESIZE_LIMIT, FILE_ROLL_COUNT, APPEND);

        // leave it up to the log levels to decide what ends up in the log file.
        fileHandler.setLevel(Level.ALL);
        fileHandler.setFormatter(new SimpleFormatter());

        Logger rootLogger = Logger.getLogger("");
        rootLogger.addHandler(fileHandler);
    }

    private static String asPath(String... elements)
    {
        StringBuffer buffer = new StringBuffer();
        String sep = "";
        for (String element : elements)
        {
            buffer.append(sep);
            buffer.append(element);
            sep = File.separator;
        }
        return buffer.toString();
    }
}
