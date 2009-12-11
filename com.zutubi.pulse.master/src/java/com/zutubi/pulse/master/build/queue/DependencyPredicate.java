package com.zutubi.pulse.master.build.queue;

/**
 * A marker interface that indicates that the predicate describes
 * a dependency of an owner, and provides details.
 */
public interface DependencyPredicate
{
    /**
     * The owner that this predicate has a dependency to.
     *
     * @return the owner.
     */
    Object getOwner();
}
