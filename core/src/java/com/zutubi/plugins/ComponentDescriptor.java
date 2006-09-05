package com.zutubi.plugins;

import nu.xom.Element;

/**
 * <class-comment/>
 */
public interface ComponentDescriptor
{
    /**
     * Configure the component descriptor using the specified xml fragment.
     *
     * @param config
     */
    void init(Element config);

    /**
     *
     */
    void destory();

    /**
     * Get the name of this component descriptor.
     *
     * @return a human readable name to identify this component descriptor.
     */
    String getName();

    /**
     * Get the key of this component descriptor.
     *
     * @return a machine readable name to identify this component descriptor.
     */
    String getKey();

    /**
     * Get a human readable description of this component descriptor.
     *
     * @return a short description of this component descriptor.
     */
    String getDescription();

    /**
     *
     * @return true iif this component descriptor is enabled.
     */
    boolean isEnabled();

    /**
     * Enable this component descriptor.
     *
     * This method should be implemented to handle the enabling of this component descriptor.
     */
    void enable();

    /**
     * Disable this component descriptor.
     *
     * This method should be implemented to handle the disabling of this component descriptor.
     */
    void disable();

    void setPlugin(Plugin plugin);
}
