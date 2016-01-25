package com.zutubi.pulse.master.build.log;

import com.zutubi.pulse.core.EventOutputStream;
import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.util.io.IOUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.text.DateFormat;
import java.util.Date;

/**
 * The base output logger implementation that supports logging to a file.
 *
 * Each line that is logged will be prefixed with a timestamp. 
 */
public abstract class AbstractFileLogger implements OutputLogger
{
    private final DateFormat FORMAT = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG);

    private final LogFile logFile;

    private final CharsetDecoder outputDecoder;
    private final char[] decodedOutput;
    private final CharBuffer decodeBuffer;
    
    private boolean isStartOfLine = true;
    private boolean lastWasCarriageReturn = false;

    protected PrintWriter writer;

    public AbstractFileLogger(LogFile logFile)
    {
        this.logFile = logFile;
        outputDecoder = Charset.defaultCharset().newDecoder();
        outputDecoder.onMalformedInput(CodingErrorAction.REPLACE);
        outputDecoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
        decodedOutput = new char[EventOutputStream.MINIMUM_SIZE];
        decodeBuffer = CharBuffer.wrap(decodedOutput);
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

            final ByteBuffer inBuffer = ByteBuffer.wrap(output, offset, length);
            CoderResult result;
            do
            {
                result = outputDecoder.decode(inBuffer, decodeBuffer, true);
                writeDecoded(marker);
            }
            while (result == CoderResult.OVERFLOW);
        }
    }

    // Marker is needed at start and between line ending and next non-line-ending.
    // Line end is needed after any line terminating character is seen:
    //   - But if've just seen \r then discard a \n.
    private void writeDecoded(String marker)
    {
        final int length = decodeBuffer.position();
        for (int i = 0; i < length; i++)
        {
            char c = decodedOutput[i];
            boolean isCarriageReturn = c == '\r';

            if (c == '\n')
            {
                if (!lastWasCarriageReturn)
                {
                    writer.println();
                    isStartOfLine = true;
                }
            }
            else if (isCarriageReturn)
            {
                writer.println();
                isStartOfLine = true;
            }
            else
            {
                if (isStartOfLine)
                {
                    writer.print(marker);
                    isStartOfLine = false;
                }

                writer.print(c);
            }

            lastWasCarriageReturn = isCarriageReturn;
        }

        writer.flush();
        decodeBuffer.clear();
    }

    protected void completeOutput()
    {
        if (writer != null)
        {
            if (!isStartOfLine)
            {
                writer.println();
            }
            writer.flush();
        }

        isStartOfLine = true;
        lastWasCarriageReturn = false;
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
