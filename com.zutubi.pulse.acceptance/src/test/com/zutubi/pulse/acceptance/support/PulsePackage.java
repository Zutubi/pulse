package com.zutubi.pulse.acceptance.support;

/**
 * A handle to the built pulse package.
 */
public interface PulsePackage
{
    /**
     * Extract the package to the specified directory, returning a handle to
     * the Pulse installation.
     *
     * @param dir   into which Pulse will be extracted.
     * @return  a handle to the Pulse installation.
     */
    Pulse extractTo(String dir);

    void setVerbose(boolean verbose);
}
