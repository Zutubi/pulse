package com.zutubi.pulse.master;

import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.util.io.IOUtils;

import java.io.*;
import java.text.DateFormat;
import java.util.Date;

public abstract class AbstractFileLogger implements OutputLogger
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
        writer = null;
    }

    public void prepare()
    {
        openWriter();
    }

    public void log(byte[] output)
    {
        if (output.length > 0)
        {
            // we want to log each line separately.
            BufferedReader reader = new BufferedReader(new StringReader(new String(output)));
            try
            {
                String line;
                while ((line = reader.readLine()) != null)
                {
                    logMarker(line);
                }
            }
            catch (IOException e)
            {
                // noop. We do not expect to have any problems reading a string.
            }
        }
    }

    public void close()
    {
        closeWriter();
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
