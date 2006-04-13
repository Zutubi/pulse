/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.logging;

import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.bootstrap.ConfigurationManager;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * <class-comment/>
 */
public class FileConfig
{
    private static final String FILE_NAME = "pulse%u.%g.log";

    private ConfigurationManager configManager;
    private static final int APPROX_FILESIZE_LIMIT = 1000000;
    private static final int FILE_ROLL_COUNT = 20;
    private static final boolean APPEND = true;

    public FileConfig() throws IOException
    {
        // hack the autowiring
        ComponentContext.autowire(this);

        File logRoot = configManager.getSystemPaths().getLogRoot();
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

/*
        // setup the CVS file logger.
        FileHandler outputHandler = new FileHandler(logRoot.getCanonicalPath() + File.separator + "cvs%u.%g.out.log");
        outputHandler.setLevel(Level.ALL);
        outputHandler.setFormatter(new NoFormatter());

        Logger outputStreamLogger = Logger.getLogger(LoggedDataOutputStream.class.getName());
        outputStreamLogger.addHandler(outputHandler);
        outputStreamLogger.setUseParentHandlers(false);

        FileHandler inputHandler = new FileHandler(logRoot.getCanonicalPath() + File.separator + "cvs%u.%g.in.log");
        inputHandler.setLevel(Level.ALL);
        inputHandler.setFormatter(new NoFormatter());

        Logger inputStreamLogger = Logger.getLogger(LoggedDataInputStream.class.getName());
        inputStreamLogger.addHandler(inputHandler);
        inputStreamLogger.setUseParentHandlers(false);
*/
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configManager = configurationManager;
    }
}
