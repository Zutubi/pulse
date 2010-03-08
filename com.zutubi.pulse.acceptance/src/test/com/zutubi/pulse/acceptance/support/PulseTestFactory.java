package com.zutubi.pulse.acceptance.support;

import java.io.File;

/**
 * Abstraction over the Python test code for access via Jython.
 */
public interface PulseTestFactory
{
    /**
     * Creates and returns a handle to a Python PulsePackage instance.  These
     * instances represent built Pulse packages that may be extracted and used
     * to run Pulse.
     * 
     * @param pkg the package file
     * @return a handle to a Python PulsePackage instance
     */
    public PulsePackage createPackage(File pkg);

    /**
     * Creates and returns a handle to a Python Pulse instance.  This instances
     * represent an unpacked Pulse installation and may be used to start and
     * stop Pulse.
     * 
     * @param pulseHome path of the pulse home directory to point at
     * @return a handle to a Python Pulse instance
     */
    public Pulse createPulse(String pulseHome);
}
