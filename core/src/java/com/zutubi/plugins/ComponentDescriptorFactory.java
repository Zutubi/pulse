package com.zutubi.plugins;

import nu.xom.Element;

import java.util.Map;

/**
 * <class-comment/>
 */
public interface ComponentDescriptorFactory
{
    /**
     * Returns true if the component descriptor factory is able to create a descriptor identified by the
     * specified type string.
     *
     * @param type represents an identifier for the component descriptor.
     *
     * @return true if this factory can create a descriptor of the specified type.
     */
    boolean supportsComponentDescriptor(String type);

    ComponentDescriptor createComponentDescriptor(String type, Element config, Plugin plugin);

    void addDescriptor(String key, Class<? extends ComponentDescriptor> descriptor);

    void setDescriptors(Map<String, Class<? extends ComponentDescriptor>> descriptors);
}
