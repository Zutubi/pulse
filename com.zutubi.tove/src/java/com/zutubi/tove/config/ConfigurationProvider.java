package com.zutubi.tove.config;

import com.zutubi.tove.config.api.Configuration;
import com.zutubi.util.NullaryFunction;

import java.util.Collection;
import java.util.Set;

/**
 * The configuration provider interface provides access to the configuration system.
 */
public interface ConfigurationProvider
{
    /**
     * Retrieve the instance identified by the specified handle.  The instance must be of the
     * type specified by the second argument.
     *
     * @param handle    the handle of the configuration instance.
     * @param clazz     is the expected type of the configuration instance.
     *
     * @return the configuration instance
     */
    <T extends Configuration> T get(long handle, Class<T> clazz);

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
     * Retrieve a configuration instance of the specified type.  Note, if there is more than one instance
     * of the specified type, no guarantee is provided regarding which of those instances will be returned.
     *
     * If no instance exists, null is returned.
     *
     * @param clazz is the type of the requested configuration instance.
     *
     * @return an instance of the requested type, or null if none exists.
     */
    <T extends Configuration> T get(Class<T> clazz);

    /**
     * Retrieve all configuration instances at paths matching the specified pattern.  All of these instances must
     * be of the specified type.  The pattern is a path which may contain wildcards.
     *
     * For example, getAll("projects/*", ProjectConfiguration.class) will return all of the ProjectConfiguration
     * instances located in the collection defined at "projects"
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
     * For example, a configuration instance that is part of a project's configuration can use this method to
     * retrieve the project that it is associated with:
     * <code>configurationProvider.getAncestorOfType(config, ProjectConfiguration.class);</code>
     *
     * @param c is the configuration instance from which the search is started.
     * @param clazz is the type of configuration instance being searched for.
     *
     * @return the located configuration instance, or null if none is found.
     */
    <T extends Configuration> T getAncestorOfType(Configuration c, Class<T> clazz);

    /**
     * Retrieves all descendents of a given path, which should all be of the specified type.  If the
     * path is within a templated scope, all instances that descend from the given path are
     * returned, potentially including the instance at that path (see the {@code strict} parameter).
     * If the path is not in a templated scope, either the empty set is returned (strict) or a set
     * containing the instance at the path (non-strict).  If the path does not exist, the empty set
     * is returned.
     *
     * @param path         path to retrieve all descendents of
     * @param clazz        type of the instances to retrieve
     * @param strict       if true, the instance at path is not included, otherwise it is included
     * @param concreteOnly if true, only concrete instances are returned
     * @return all instances that descend from the given path and match the filtering criteria
     *
     * @throws IllegalArgumentException if the given path references an invalid scope or any of the
     *         located instances do not match the given type
     */
    <T extends Configuration> Set<T> getAllDescendents(String path, Class<T> clazz, boolean strict, boolean concreteOnly); 

    /**
     * Indicates if an instance and all instances reachable via its
     * properties are valid.  For example, for a project, indicates if the
     * entire project configuration (including the SCM, triggers etc) is
     * valid.
     *
     * @param path the path to test
     * @return true if all instances under the path are valid
     */
    boolean isDeeplyValid(String path);

    /**
     * Register a configuration event listener.  This listener will receive events from
     * configuration instances identified by the defined paths.  If includeChildPaths is true, then
     * changes to child configuration instances (those that are nested under the given paths) will
     * also be passed through to the listener.
     *
     * @param listener          the event listener to register
     * @param synchronous       indicates whether or not the event notification should be on the
     *                          same thread that generated the event (synchronous), or a separate
     *                          thread.  Synchronous event handlers must be careful not to take up
     *                          too much time.
     * @param includeChildPaths indicates whether or not to include notifications for nested paths
     * @param paths             the paths the event listener will be listening to
     */
    void registerEventListener(ConfigurationEventListener listener, boolean synchronous, boolean includeChildPaths, String... paths);
    void registerEventListener(ConfigurationEventListener listener, boolean synchronous, boolean includeChildPaths, Class<? extends Configuration> clazz);

    /**
     * Register an event listener to be triggered whenever an instance of the specified class is the
     * subject of a configuration event.
     *
     * @param listener    the listener to register
     * @param synchronous if true, the listener is called synchronously during the event publishing,
     *                    otherwise it is invoked at some later time in a separate thread
     * @param clazz       the class to listen to events for -- all instances of this type (including
     *                    subtypes) will trigger callbacks
     */
    void registerEventListener(ConfigurationEventListener listener, boolean synchronous, Class<? extends Configuration> clazz);

    void unregisterEventListener(ConfigurationEventListener listener);

    // should these methods be here or somewhere else?
    <T extends Configuration> T deepClone(T instance);
    String insert(String path, Configuration instance);
    String save(Configuration instance);
    void delete(String path);
    <T> T executeInsideTransaction(NullaryFunction<T> f);
}
