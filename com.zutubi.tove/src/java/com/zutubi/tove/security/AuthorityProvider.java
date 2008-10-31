package com.zutubi.tove.security;

import java.util.Set;

/**
 * Used to determine which authorities are allowed to perform actions on
 * resources.
 */
public interface AuthorityProvider<T>
{
    /**
     * Returns the set of authorities that are allowed to perform the given
     * action on the given object.
     *
     * @param action   the action requested
     * @param resource the object the action is requested on
     * @return the set of authorities that are allowed to perform the action
     */
    Set<String> getAllowedAuthorities(String action, T resource);
}
