package com.zutubi.pulse.master.build.log;

import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.util.io.IOUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;

/**
 * The base output logger implementation that supports logging to a file.
 *
 * Each line that is logged will be prefixed with a timestamp. 
 */
public abstract class AbstractFileLogger implements OutputLogger
{
    private static final DateFormat FORMAT = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG);

    private LogFile logFile;

    private byte lastByte = '\n';

    protected PrintWriter writer;

    public AbstractFileLogger(LogFile logFile)
    {
        this.logFile = logFile;
    }

    public void openWriter()
    {
        try
        {
            writer = new PrintWriter(logFile.openWriter());
        }
        catch (IOException e)
        {
            throw new BuildException("Unable to open build log file: " + e.getMessage(), e);
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
        log(output, 0, output.length);
    }

    public void log(byte[] output, int offset, int length)
    {
        if (length > 0 && writer != null)
        {
            long timestamp = System.currentTimeMillis();
            String marker = getMarker(timestamp);
            if (lastByte == '\n' || (lastByte == '\r' && output[0] != '\n'))
            {
                writer.print(marker);
            }

            String s = new String(output, offset, length);
            String markerReplacement = "$1" + marker + "$2";
            String stamped = s.replaceAll("(\\r\\n?|\\n)(.)", markerReplacement);
            writer.print(stamped);
            writer.flush();

            lastByte = output[output.length - 1];
        }
    }

    protected void completeOutput()
    {
        if ((lastByte != '\r' && lastByte != '\n') && writer != null)
        {
            writer.println();
            writer.flush();
        }

        lastByte = '\n';
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
        logMarker(message, time, true);
    }
    
    protected void logMarker(String message, long time, boolean newline)
    {
        if (writer != null)
        {
            writer.print(getMarker(time));
            writer.print(message);
            if (newline)
            {
                writer.println();
            }
            writer.flush();
        }
    }

    private String getMarker(long time)
    {
        return FORMAT.format(new Date(time)) + ": ";
    }
}
