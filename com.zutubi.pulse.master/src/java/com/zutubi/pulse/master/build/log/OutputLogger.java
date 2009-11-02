package com.zutubi.pulse.master.build.log;

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

    /**
     * Log the data to the output.
     *
     * @param output  the data to be logged.
     */
    void log(byte[] output);

    /**
     * Log the specified portion of the data array to the output
     *
     * @param source the raw data array.
     * @param offset the starting offset for the data that will be logged.
     * @param length the length of the data that will be logged.
     */
    void log (byte[] source, int offset, int length);
}
