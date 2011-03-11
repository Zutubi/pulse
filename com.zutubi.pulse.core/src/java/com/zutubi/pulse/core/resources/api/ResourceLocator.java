package com.zutubi.pulse.core.resources.api;

import java.util.List;

/**
 * <p>
 * Interface for classes that can automatically discover installed resources.
 * A locator may, for example, find an installation of Apache Ant and create a
 * matching resource which will be picked up automatically by Ant projects.
 * </p>
 * <p>
 * Resource discovery occurs on each agent when the agent comes online.  The
 * process should therefore be reasonably fast -- e.g. it is not acceptable to
 * search large areas of the file system looking for resources.  As a general
 * rule, no locator should take more than a few seconds to run.
 * </p>
 * <p>
 * Resources located in this way will not overwrite those configured by the
 * user.  Instead, non-conflicting parts will be merged in to existing
 * configuration where possible, and other parts will be ignored.
 * </p>
 * <p>
 * Instead of implementing this class directly, consider subclassing one of the
 * support classes.
 * </p>
 */
public interface ResourceLocator
{
    /**
     * Locates and returns information about resources on the local machine.
     * Implementations of this method should limit their running time to at
     * most a few seconds.  It is not advisable, for example, to scan large
     * areas of the file system.  Rather, searches should be restricted to
     * probable install locations.
     * 
     * @return a list of all resources located
     */
    List<ResourceConfiguration> locate();
}
