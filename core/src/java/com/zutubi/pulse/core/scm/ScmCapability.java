package com.zutubi.pulse.core.scm;

/**
 * SCM capabilities are used to indicate what operations an SCM
 * implementation supports.
 */
public enum ScmCapability
{
/*
    BROWSE,
    CHECKOUT_FILE,
    CHECKOUT_AT_REVISION,
    LATEST_REVISION,
    LIST_CHANGES,
    POLL,
    TAG,
    UPDATE
*/
    /**
     * This defines whether or not the contents of the scm repository can be browsed / navigated.
     * This involves being able obtain a listing of remote directories.
     */
    BROWSE,

    /**
     * This scm capability defines whether or not the scm supports defining the changes that have occured
     * at each revision.  This is used by pulse to provide extra information about what changes have
     * occured in a build.
     */
    CHANGESET,

    /**
     * This scm capability defines whether or not tagging of the scm server is supported.  Tagging of
     * the scm server is analogous to adding a persistent label to a revision on the scm server so that
     * later you can retrieve that revision using the label.  This is typically done as a post build
     * step.
     */
    TAG,

    /**
     * This scm capability defines whether or not this implementation is able to determine the state of
     * a local scm directory.  This is used by the personal build process to determine which files have
     * been changed and therefore need to be added to the patch sent to the pulse server for building
     */
    PATCH
}
