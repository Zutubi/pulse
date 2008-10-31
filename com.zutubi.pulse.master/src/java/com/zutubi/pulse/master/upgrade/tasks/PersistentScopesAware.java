package com.zutubi.pulse.master.upgrade.tasks;

/**
 * Interface for instances that are aware of the persistent scopes in which
 * they are operating, usually because they need templating information to
 * perform their job.
 */
public interface PersistentScopesAware
{
    /**
     * Used to wire in information about the scopes in which the upgrade is
     * taking place.  The wiring happens once before the instance is used for
     * its primary purpose.
     *
     * @param persistentScopes information about the scopes in which the
     *                         upgrade is taking place
     */
    void setPersistentScopes(PersistentScopes persistentScopes);
}
