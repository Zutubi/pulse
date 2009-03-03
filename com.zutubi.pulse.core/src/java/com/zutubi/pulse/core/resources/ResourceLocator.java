package com.zutubi.pulse.core.resources;

import com.zutubi.pulse.core.config.ResourceConfiguration;

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
    List<ResourceConfiguration> locate();
}
