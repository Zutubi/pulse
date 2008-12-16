package com.zutubi.pulse.master;

import java.io.Closeable;

/**
 * The output logger defines a sink for output messages generated during the
 * various stages of a build.
 */
public interface OutputLogger extends Closeable
{
    /**
     * Initialise any required resources.  This method will be called before any logging
     * requestes are made.
     */
    void prepare();

    void log(byte[] output);
}
