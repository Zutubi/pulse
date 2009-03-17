package com.zutubi.pulse.acceptance.support;

import java.io.File;
import java.io.Closeable;

/**
 * A factory for PulsePackage instances.
 */
public interface PackageFactory extends Closeable
{
    /**
     * Create a new PulsePackage instance from the pulse distribution file.
     *
     * @param pkg   a pulse package.
     * @return  a handle to a Pulse package implementation.
     */
    public PulsePackage createPackage(File pkg);
}
