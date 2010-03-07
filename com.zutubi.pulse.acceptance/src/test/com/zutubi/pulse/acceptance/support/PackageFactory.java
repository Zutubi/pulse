package com.zutubi.pulse.acceptance.support;

import java.io.File;

/**
 *
 *
 */
public interface PackageFactory
{
    public PulsePackage createPackage(File pkg);

    public Pulse createPulse(String pulseHome);

    void close();
}
