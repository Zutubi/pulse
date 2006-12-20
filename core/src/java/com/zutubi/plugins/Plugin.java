package com.zutubi.plugins;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * <class-comment/>
 */
public interface Plugin
{
    /**
     * Get the machine readable name for this plugin.  The key uniquely identifies this plugin instance.
     *
     * @return the key for this plugin.
     */
    String getKey();

    /**
     * Get the human readable name for this plugin.
     *
     * @return the name for this plugin.
     */
    String getName();

    /**
     *
     * @return true if this plugin instance is enabled, false otherwise.
     */
    boolean isEnabled();

    /**
     * Enable this plugin.
     */
    void enable();

    /**
     * Disable this plugin.
     */
    void disable();

    /**
     *
     * @return true if this plugin instance is marked as hidden, false otherwise.
     */
    boolean isHidden();

    /**
     * Get the plugin instances info.
     *
     * @return
     *
     * @see PluginInformation
     */
    PluginInformation getInfo();

    /**
     * Get the list of component descriptors defined by this plugin.
     *
     */
    List<ComponentDescriptor> getComponentDescriptors();

    /**
     * Get the source URL for this plugin.  The source of the plugin represents the location from which it was loaded,
     * if one is available.
     *
     */
    URL getSource();

    Class loadClass(String className, Class callingClass) throws ClassNotFoundException;

    InputStream getResourceAsStream(String resourceName);

    void close();
}
