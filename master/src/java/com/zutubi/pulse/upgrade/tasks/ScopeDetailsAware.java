package com.zutubi.pulse.upgrade.tasks;

/**
 * Interface for instances that are aware of the scope in which they are
 * operating, usually because they need templating information to perform their
 * job.
 */
public interface ScopeDetailsAware
{
    /**
     * Used to wire in information about the scope in which the upgrade is
     * taking place.  The wiring happens once before the instance is used for
     * its primary purpose.
     *
     * @param scopeDetails information about the scope in which the upgrade is
     *                     taking place
     */
    void setScopeDetails(ScopeDetails scopeDetails);
}
