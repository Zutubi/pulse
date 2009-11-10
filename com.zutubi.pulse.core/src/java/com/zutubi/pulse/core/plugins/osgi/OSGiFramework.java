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

    /**
     * When set to true, this property indicates that the path identified by the OSGI_CONFIGURATION_AREA
     * is read only. 
     */
    static final String OSGI_CONFIGURATION_AREA_READONLY = OSGI_CONFIGURATION_AREA + ".readOnly";
}
