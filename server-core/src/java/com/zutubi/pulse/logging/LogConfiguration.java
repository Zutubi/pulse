package com.zutubi.pulse.logging;

/**
 * <class-comment/>
 */
public interface LogConfiguration
{
    public static final String LOGGING_CONFIG = "log.config";

    String getLoggingLevel();

    void setLoggingLevel(String lvl);
}
