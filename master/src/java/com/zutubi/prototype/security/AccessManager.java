package com.zutubi.prototype.security;

/**
 * Interface for managing access to protected resources.  The manager
 * determines if a given actor has the authority to perform a given action on
 * a given resource.
 */
public interface AccessManager
{
    public static final String ACTION_ADMINISTER = "administer";
    public static final String ACTION_CREATE = "create";
    public static final String ACTION_DELETE = "delete";
    public static final String ACTION_READ = "read";
    public static final String ACTION_WRITE = "write";

    /**
     * Determines if the given actor has permission to perform the given
     * action on the given resource.
     *
     * @param actor    the actor trying to perform the action
     * @param action   the action the user wants to perform
     * @param resource the resource being acted on, may be null for a
     *                 "global" action
     * @return true iff the user has permission to perform the action
     */
    boolean hasPermission(Actor actor, String action, Object resource);
}
