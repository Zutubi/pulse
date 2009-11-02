package com.zutubi.pulse.master.build.log;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An output stream that writes directly to an output log.
 */
public class OutputLoggerOutputStream extends OutputStream
{
    private OutputLogger logger;

    public OutputLoggerOutputStream(OutputLogger logger)
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

    public void write(byte[] source, int offset, int length) throws IOException
    {
        logger.log(source, offset, length);
    }
}
