package com.zutubi.pulse.core.util.process;

/**
 * A byte handler that does nothing with the handled bytes - a sink for output.
 */
public class NullByteHandler implements ByteHandler
{
    public void handle(byte[] buffer, int n, boolean error) throws Exception
    {
    }
}
