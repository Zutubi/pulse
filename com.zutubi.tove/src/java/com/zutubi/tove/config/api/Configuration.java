package com.zutubi.tove.config.api;

import com.zutubi.tove.annotations.Transient;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Basic interface that must be implemented by all configuration types.
 * <p>
 * Configurations are essentially data holders with some added properties as listed below:
 * <ul>
 * <li>permanent</li> a permanent configuration instance can not be deleted.
 * <li>handle</li> the unique identifier for this configuration instance.  This identifier
 * does not change over time.
 * <li>concrete</li> indicates whether or not the configuration is a concrete node in the
 * templated hierarchy. Only concrete nodes are instantiated into configuration instances.
 * <li>configuration path</li> a string that uniquely identifies the configuration instance
 * within the configuration hierarchy. For instance, 'users/John' uniquely identifies the
 * user 'John'. Note that the path of a configuration instance may change over time.
 * </ul>
 * <p>
 * The meta properties are used to hold descriptive information about a configuration instance.
 * For instance, the handle property value is stored as a configuration meta property.
 */
public interface Configuration
{
    /**
     * The value representing an undefined handle value.
     */
    static final long UNDEFINED = 0;

    /**
     * The internal meta property key for the handle property.
     */
    static final String HANDLE_KEY    = "handle";
    /**
     * The internal meta property key for the permanent property.
     */
    static final String PERMANENT_KEY = "permanent";

    /**
     * Retrieve the named meta property.
     * @param key   the key identifying the meta property.
     * @return the value of the meta property, or null if the property does not exist.
     */
    String getMeta(String key);
    void putMeta(String key, String value);
    Set<String> metaKeySet();

    /**
     * @return the unique identifier for this configuration instance.
     */
    @Transient
    long getHandle();
    void setHandle(long handle);

    @Transient
    String getConfigurationPath();
    void setConfigurationPath(String configurationPath);

    /**
     * @return true if this configuration instance is marked as concrete.
     */
    @Transient
    boolean isConcrete();
    void setConcrete(boolean concrete);

    /**
     * @return true if this configuration instance is marked as permanent.
     */
    @Transient
    boolean isPermanent();

    /**
     * Mark this configuration instance as permanent.
     *
     * @param permanent if true, this instance is marked as permanent.
     */
    void setPermanent(boolean permanent);

    /**
     * @return true if there are no validation errors recorded directly on
     *         this instance.
     */
    @Transient
    boolean isValid();

    /**
     * @return true if this instance has not yet been validated
     */
    boolean needsValidation();

    /**
     * Mark this instance as validated, so it no longe {@link #needsValidation()}.
     */
    void validated();

    /**
     * @return the list of errors associated directly with the instance, not with any particular field.
     * This will return an empty list if there are no instance errors.
     */
    @Transient
    List<String> getInstanceErrors();

    /**
     * Add an instance error message to this configuration.
     *
     * @param message is a human readable error message.
     */
    void addInstanceError(String message);

    /**
     * Clear all instance error messages from this configuration.
     */
    void clearInstanceErrors();

    /**
     * @return the map of field names to error message lists for this configuration.
     */
    @Transient
    Map<String, List<String>> getFieldErrors();
    void clearFieldErrors();

    /**
     * @param field the name of the field being queried.
     * @return the list of error messages associated with the specified field.
     */
    @Transient
    List<String> getFieldErrors(String field);
    void addFieldError(String field, String message);
    void clearFieldErrors(String field);
}
