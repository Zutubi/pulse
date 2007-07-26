package com.zutubi.pulse.util.process;

/**
 */
public interface ByteHandler
{
    void handle(byte[] buffer, int n, boolean error) throws Exception;
}
