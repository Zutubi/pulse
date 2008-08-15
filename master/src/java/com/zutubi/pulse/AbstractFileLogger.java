package com.zutubi.pulse;

import com.zutubi.pulse.core.BuildException;
import com.zutubi.util.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;

/**
 *
 */
public abstract class AbstractFileLogger
{
    private static final DateFormat FORMAT = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG);

    private File logFile;

    protected PrintWriter writer;

    public AbstractFileLogger(File logFile)
    {
        this.logFile = logFile;
    }

    public void openWriter()
    {
        try
        {
            writer = new PrintWriter(logFile);
        }
        catch (FileNotFoundException e)
        {
            throw new BuildException("Unable to create build log file '" + logFile.getAbsolutePath() + "': " + e.getMessage(), e);
        }
    }

    public void closeWriter()
    {
        IOUtils.close(writer);
    }

    protected void logMarker(String message)
    {
        logMarker(message, System.currentTimeMillis());
    }

    protected void logMarker(String message, long time)
    {
        if (writer != null)
        {
            writer.print(FORMAT.format(new Date(time)));
            writer.print(": ");
            writer.println(message);
            writer.flush();
        }
    }
}
