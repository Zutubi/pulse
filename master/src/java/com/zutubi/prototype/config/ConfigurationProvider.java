package com.zutubi.prototype.config;

import com.zutubi.pulse.core.config.Configuration;

import java.util.Collection;

/**
 * The configuration provider interface provides access to the configuration system.
 */
public interface ConfigurationProvider
{
    /**
     * Retrieve the instance located at the specified path.  The instance must be of the type
     * specified by the second argument.
     * 
     * @param path of the configuration instance being looked up.  This path can not contain any wild cards.
     * @param clazz is the expected type of the configuration instance.
     *
     * @return the configuration instance.
     */
    <T extends Configuration> T get(String path, Class<T> clazz);

    /**
     * Retrieve a configuration instance of the specified type.  Note, if there are more than one instance
     * of the specified type, no guarentees are provided on which of those instances will be returned.
     *
     * If no instance exists, null is returned.
     *
     * @param clazz is the type of the requested configuration instance.
     *
     * @return an instance of the requested type, or null if none exists.
     */
    <T extends Configuration> T get(Class<T> clazz);

    /**
     * Retrieve all of the instances of the configuration instances at the specified path.  All of these instances must
     * be of the specified type.  The path may contain wildcards.
     *
     * For example, getAll("projects/*", ProjectConfiguration.class) will return all of the ProjectConfiguration
     * instances located in the collection defined at "project/*"
     *
     * @param path of the configuration instances being looked up.  This path can contain wild card characters.
     * @param clazz is the expected type of the configuration instances.
     *
     * @return the collection of configuration instances. If none were located, then an empty collection is returned.  
     */
    <T extends Configuration> Collection<T> getAll(String path, Class<T> clazz);

    /**
     * Retrieve all of the configuration instances of the specified type.
     *  
     * @param clazz is the type of the requested configuration instance.
     *
     * @return a collection of configuration instances.
     */
    <T extends Configuration> Collection<T> getAll(Class<T> clazz);

    /**
     * Retrieve the ancestor of the specific configuration instance that is of the specified type.
     *
     * For example, a configuration instance that is part of a projects configuration can use this method to
     * retrieve the project that it is associated with.
     * <code>configurationProvider.getAncestorOfType(config, ProjectConfiguration.class);</code>
     *
     * @param c is the configuration instance from which the search is started.
     * @param clazz is the type of configuration instance being searched for.
     *
     * @return the located configuration instance, or null if none is found.
     */
    <T extends Configuration> T getAncestorOfType(Configuration c, Class<T> clazz);

    /**
     * Register a configuration event listener.  This listener will receive events from configuration instances identified
     * by the defined paths.  If includeChildPaths is true, then changes to child configuration instances will also be
     * passed through to the registered listener.
     *
     * @param listener the event listener that will receive the event notifications.
     * @param synchronous indicates whether or not the event notification should be on the same thread that generated
     * the event (synchronous), or a separate thread.  Synchronous event handlers must be careful not to take up too
     * much time.
     * @param includeChildPaths indicates whether or not to include the child configurations.
     * @param paths defines the paths the event listener will be listening to.
     */
    void registerEventListener(ConfigurationEventListener listener, boolean synchronous, boolean includeChildPaths, String... paths);
    void registerEventListener(ConfigurationEventListener listener, boolean synchronous, boolean includeChildPaths, Class clazz);

    /**
     * Register an event listener to be triggered whenever an instance of the specified class is the subject of the
     * event.
     *
     * @param listener to be triggered.
     * @param synchronous
     * @param clazz
     */
    void registerEventListener(ConfigurationEventListener listener, boolean synchronous, Class clazz);

    void unregisterEventListener(ConfigurationEventListener listener);

    // should these methods be here or somewhere else?
    String insert(String path, Object instance);
    void save(String path, Object instance);
    void delete(String path);
}
