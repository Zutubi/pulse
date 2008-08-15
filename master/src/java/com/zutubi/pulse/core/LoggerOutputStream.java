package com.zutubi.pulse.core;

import com.zutubi.pulse.OutputLogger;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An output stream that writes directly to an output log.
 */
public class LoggerOutputStream extends OutputStream
{
    private OutputLogger logger;

    public LoggerOutputStream(OutputLogger logger)
    {
        this.logger = logger;
    }

    protected void sendEvent(byte[] sendBuffer)
    {
        logger.log(sendBuffer);
    }

    public void write(int b) throws IOException
    {
        logger.log(new byte[]{(byte)b});
    }

    public void write(byte b[], int off, int len) throws IOException
    {
        logger.log(b, off, len);
    }
}
