package com.zutubi.pulse.core.util.process;

/**
 */
public interface ByteHandler
{
    void handle(byte[] buffer, int n, boolean error) throws Exception;
}
