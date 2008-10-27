package com.zutubi.tove.config.api;

import com.zutubi.tove.annotations.Transient;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Basic interface that must be implemented by all configuration types.
 */
public interface Configuration
{
    static final String HANDLE_KEY    = "handle";
    static final String PERMANENT_KEY = "permanent";

    String getMeta(String key);
    void putMeta(String key, String value);
    Set<String> metaKeySet();

    @Transient
    long getHandle();
    void setHandle(long handle);

    @Transient
    String getConfigurationPath();
    void setConfigurationPath(String configurationPath);

    @Transient
    boolean isConcrete();
    void setConcrete(boolean concrete);

    @Transient
    boolean isPermanent();
    void setPermanent(boolean permanent);

    /**
     * @return true if there are no validation errors recorded directly on
     *         this instance.
     */
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
