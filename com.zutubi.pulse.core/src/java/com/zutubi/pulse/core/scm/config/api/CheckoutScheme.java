package com.zutubi.pulse.core.scm.config.api;

/**
 * Defines how the source is checked out of an SCM for a build.
 */
public enum CheckoutScheme
{
    /**
     * Always checkout a fresh copy of the project to the base directory.
     */
    CLEAN_CHECKOUT,

    /**
     * Keep a local copy of the project, update it to the required
     * revision and copy to a clean base directory to build.
     */
    CLEAN_UPDATE,

    /**
     * Keep a copy of the project, update to the required revision and
     * build in place.
     */
    INCREMENTAL_UPDATE
}
