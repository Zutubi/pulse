package com.zutubi.pulse.master;

/**
 */
public interface OutputLogger
{
    void log(byte[] output);
    void log(byte[] output, int offset, int length);
}
