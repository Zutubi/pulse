package com.zutubi.tove.security;

import java.util.Set;

/**
 * An actor is some entity that is granted authorities to perform certain
 * actions, e.g. a user.
 */
public interface Actor
{
    String getUsername();
    Set<String> getGrantedAuthorities();
    /**
     * Indicates if this actor represents an anonymous user.
     *
     * @return true if the actor is anonymous, false if they have an identity
     */
    boolean isAnonymous();
}
