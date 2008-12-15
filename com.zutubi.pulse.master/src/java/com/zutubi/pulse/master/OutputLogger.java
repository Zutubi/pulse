package com.zutubi.pulse.master;

/**
 * The output logger defines a sink for output messages generated during the
 * various stages of a build.
 */
public interface OutputLogger
{
    /**
     * Initialise any required resources.  This method will be called before any logging
     * requestes are made.
     */
    void prepare();

    void log(byte[] output);

    /**
     * Close any held resources.  This method will be called after the final logging
     * request is made.
     */
    void close();
}
