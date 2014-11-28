package com.zutubi.pulse.core.plugins.osgi;

/**
 * This interface defines some of the properties that are defined by OSGI.
 */
public interface OSGiFramework
{
    /**
     * The configuration location for the platform. The configuration determines what plug-ins
     * will run as well as various other system settings.
     */
    static final String OSGI_CONFIGURATION_AREA = "osgi.configuration.area";
}
