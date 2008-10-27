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
}
