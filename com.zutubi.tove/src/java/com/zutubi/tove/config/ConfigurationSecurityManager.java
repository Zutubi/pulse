package com.zutubi.tove.config;

import com.zutubi.events.Event;
import com.zutubi.events.EventManager;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.events.ConfigurationSystemStartedEvent;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.logging.Logger;
import org.acegisecurity.AccessDeniedException;

import java.util.*;
import java.util.logging.Level;

/**
 * Manages access to the configuration system.  Allows UIs to filter what a
 * user can see and do to configuration.
 */
public class ConfigurationSecurityManager implements com.zutubi.events.EventListener
{
    private static final Logger LOG = Logger.getLogger(ConfigurationSecurityManager.class);

    private List<PathPermission> globalPermissions = new LinkedList<PathPermission>();
    private Set<String> ownedScopes = new HashSet<String>();

    private AccessManager accessManager;
    private ConfigurationTemplateManager configurationTemplateManager;
    private boolean configurationSystemStarted;

    /**
     * Registers a mapping from an action on some path to a pre-defined
     * global action.  When checking if the action can be performed on the
     * path, the global action is checked.  For example, creating a
     * "FooConfiguration" (action "create" on path "foo") may be mapped to a
     * global "CREATE_FOO" action.
     *
     * @param pathPattern  path pattern for the action to be mapped
     * @param action       the action to be mapped
     * @param globalAction the global action that should be used in place of
     *                     the normal path/action check
     */
    public void registerGlobalPermission(String pathPattern, String action, String globalAction)
    {
        globalPermissions.add(new PathPermission(pathPattern, action, globalAction));
    }

    /**
     * Registers a scope where nested configuration is owned by a parent
     * instance.  For example, the "foo" scope may be a collection of
     * FooConfigurations, where all paths under "foo/id" are owned by the
     * FooConfiguration instance at that path.  Thus a write to
     * "foo/id/bar" is allowed iff the actor has permission to perform
     * writes on the resource "foo/id" itself.  Non-owned scopes use global
     * permissions: the actor must have global permission to perform a
     * requested action.
     *
     * @param scope the scope to be marked as owned
     */
    public void registerOwnedScope(String scope)
    {
        ownedScopes.add(scope);
    }

    /**
     * Tests if the calling thread has permission to perform the given action
     * on the given path, and if not throws an AccessDeniedException.
     *
     * @see #hasPermission(String, String)
     *
     * @param path   the path being acted upon
     * @param action the action to be carried out
     * @throws AccessDeniedException if the logged in actor does not have
     *         permission to perform the specified action on the specified path
     */
    public void ensurePermission(String path, String action)
    {
        if(!hasPermission(path, action))
        {
            throw new AccessDeniedException("Permission to " + action + " at path '" + path + "' denied");
        }
    }

    /**
     *
     * Checks if the calling thread has permission to perform the given action
     * on the given path.  If the path is in an owned scope (see
     * {@link #registerOwnedScope(String)}, then this amounts to checking if
     * the thread has permission to perform the action on the resource that
     * owns the path.  For non-owned scopes, the thread must have permission
     * to perform the action globally.
     *
     * Note that special access rules apply in two circumstances:
     * <ol>
     *   <li>When a matching global permission is found (see
     *   {@link #registerGlobalPermission(String, String, String)}, that
     *   permission is used in place of the original path/action pair when
     *   doing the access check.</li>
     *
     *   <li>When reading a path in a templated scope, access may be granted
     *   because the actor has permission to read a template descendant of
     *   the given path.  This ensures that an actor can read all ancestors
     *   of any resources they can read (which they could implicitly anyway).</li>
     * </ol>
     *
     * @param path   the path being acted upon
     * @param action the action to be carried out
     * @return true iff the logged in actor has permission to perform the
     *         specified action on the specified path
     */
    public boolean hasPermission(String path, String action)
    {
        if(!configurationSystemStarted)
        {
            return true;
        }

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest(String.format("hasPermission('%s', '%s')", path, action));
        }

        String global = getGlobalPermission(path, action);
        if(global == null)
        {
            if(configurationTemplateManager.isPersistent(path))
            {
                if(AccessManager.ACTION_VIEW.equals(action) && configurationTemplateManager.isTemplatedPath(path))
                {
                    // Readability is subject to special rules in templated
                    // scopes: if you can read a template you can also read
                    // all of it's ancestors.
                    Configuration resource = findOwningResource(path);
                    if(accessManager.hasPermission(action, resource))
                    {
                        LOG.finest("Permission granted directly on owning resource");
                        return true;
                    }

                    // No direct permission, see if any descendants have permission.
                    if (resource != null)
                    {
                        for(String descendantPath: configurationTemplateManager.getDescendantPaths(resource.getConfigurationPath(), true, false, false))
                        {
                            Configuration instance = configurationTemplateManager.getInstance(descendantPath);
                            if(instance != null && accessManager.hasPermission(action, instance))
                            {
                                LOG.finest("Permission reverse inherited via descendant");
                                return true;
                            }
                        }
                    }

                    LOG.finest("Permission denied");
                    return false;
                }
                else
                {
                    if(AccessManager.ACTION_CREATE.equals(action) || AccessManager.ACTION_DELETE.equals(action))
                    {
                        // Create and delete permissions are translated to a write to
                        // the parent path (which is the path passed through for create).
                        action = AccessManager.ACTION_WRITE;
                        if(AccessManager.ACTION_DELETE.equals(action))
                        {
                            path = PathUtils.getParentPath(path);
                        }
                    }

                    Configuration resource = findOwningResource(path);
                    boolean result = accessManager.hasPermission(action, resource);
                    if(LOG.isLoggable(Level.FINEST))
                    {
                        if(result)
                        {
                            LOG.finest("Permission granted");
                        }
                        else
                        {
                            LOG.finest("Permission denied");
                        }
                    }

                    return result;
                }
            }
            else
            {
                // Transient paths are a free-for-all.
                LOG.finest("Permission granted for transient path");
                return true;
            }
        }
        else
        {
            boolean result = accessManager.hasPermission(global, null);
            if(LOG.isLoggable(Level.FINEST))
            {
                String message = String.format("Permission %s via global permission '%s'", result ? "granted" : "denied", global);
                LOG.finest(message);
            }
            return result;
        }
    }

    private String getGlobalPermission(String path, String action)
    {
        for(PathPermission pp: globalPermissions)
        {
            if(pp.matches(path, action))
            {
                return pp.getGlobalAction();
            }
        }

        return null;
    }

    /**
     * Filters the given listing of child paths in-place so that only paths
     * that the current thread has permission to perform the given action to
     * are left.
     *
     * @see #hasPermission(String, String)
     *
     * @param prefix the prefix path that the child paths are under
     * @param paths  list of child paths to filter: this list is modified!
     * @param action the action to filter by
     * @return the passed in list, which will have been modified in place
     */
    public List<String> filterPaths(String prefix, List<String> paths, String action)
    {
        Iterator<String> it = paths.iterator();
        while(it.hasNext())
        {
            String path = it.next();
            if(!hasPermission(PathUtils.getPath(prefix, path), action))
            {
                it.remove();
            }
        }

        return paths;
    }

    /**
     * Determines the resource that owns the given path.  For example, paths
     * into a project are owned by the project instance and thus controlled
     * by that project's ACLs.  Where no ACLs apply, no owning resource
     * exists and null is returned.  In this case global permissions are
     * applied.
     *
     * @param path the path to find the owner of
     * @return the object whose ACLs should apply to the path, or null if no
     *         such resource exists
     */
    public Configuration findOwningResource(String path)
    {
        String[] elements = PathUtils.getPathElements(path);
        if(elements.length > 1 && ownedScopes.contains(elements[0]))
        {
            return configurationTemplateManager.getInstance(PathUtils.getPath(elements[0], elements[1]));
        }
        else
        {
            return null;
        }
    }

    public void handleEvent(Event event)
    {
        configurationSystemStarted = true;
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{ConfigurationSystemStartedEvent.class };
    }

    public void setAccessManager(AccessManager accessManager)
    {
        this.accessManager = accessManager;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }

    private static class PathPermission
    {
        private String pathPattern;
        private String action;
        private String globalAction;

        public PathPermission(String pathPattern, String action, String globalAction)
        {
            this.pathPattern = pathPattern;
            this.action = action;
            this.globalAction = globalAction;
        }

        public String getPathPattern()
        {
            return pathPattern;
        }

        public String getAction()
        {
            return action;
        }

        public String getGlobalAction()
        {
            return globalAction;
        }

        public boolean matches(String path, String action)
        {
            return action.equals(this.action) && PathUtils.pathMatches(pathPattern, path);
        }
    }

    public void setEventManager(EventManager eventManager)
    {
        eventManager.register(this);
    }
}
