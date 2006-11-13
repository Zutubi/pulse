package com.zutubi.pulse.logging;

/**
 * <class-comment/>
 */
public interface LogConfiguration
{
    public static final String LOGGING_CONFIG = "log.config";

    public static final String LOG_EVENTS = "log.events";

    String getLoggingLevel();

    void setLoggingLevel(String lvl);

    boolean isEventLoggingEnabled();

    void setEventLoggingEnabled(boolean b);
}
