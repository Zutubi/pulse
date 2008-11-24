package com.zutubi.pulse.master;

import java.io.File;

/**
 */
public class AbstractOutputLogger extends AbstractFileLogger implements OutputLogger
{
    public AbstractOutputLogger(File logFile)
    {
        super(logFile);
    }

    public void log(byte[] output)
    {
        log(output, 0, output.length);
    }

    public void log(byte[] output, int offset, int length)
    {
        if (length > 0)
        {
            writer.write(new String(output, offset, length));
            writer.flush();
        }
    }
}
