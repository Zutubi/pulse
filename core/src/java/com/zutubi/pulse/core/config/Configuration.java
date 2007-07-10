package com.zutubi.pulse.core.config;

import com.zutubi.config.annotations.Transient;

import java.util.Map;
import java.util.List;

/**
 * Basic interface that must be implemented by all configuration types.
 */
public interface Configuration
{
    @Transient
    long getHandle();
    void setHandle(long handle);

    @Transient
    String getConfigurationPath();
    void setConfigurationPath(String configurationPath);

    @Transient
    boolean isValid();

    @Transient
    List<String> getInstanceErrors();
    void addInstanceError(String message);
    void clearInstanceErrors();

    @Transient
    Map<String, List<String>> getFieldErrors();
    void clearFieldErrors();

    @Transient
    List<String> getFieldErrors(String field);
    void addFieldError(String field, String message);
    void clearFieldErrors(String field);
}
