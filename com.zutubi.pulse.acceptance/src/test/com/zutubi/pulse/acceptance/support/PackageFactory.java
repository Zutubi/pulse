package com.zutubi.pulse.acceptance.support;

import java.io.File;

/**
 * A factory for PulsePackage instances.
 */
public interface PackageFactory
{
    /**
     * Create a new PulsePackage instance from the pulse distribution file.
     *
     * @param pkg   a pulse package.
     * @return  a handle to a Pulse package implementation.
     */
    public PulsePackage createPackage(File pkg);

    /**
     * Free up the resources held by this factory.
     */
    void close();
}
