package com.zutubi.pulse.plugins.osgi;

/**
 *
 *
 */
public interface OSGiFramework
{
    static final String OSGI_CONFIGURATION_AREA = "osgi.configuration.area";

    void start() throws Exception;

    void stop() throws Exception;

    void setProperty(String key, String value);
}
