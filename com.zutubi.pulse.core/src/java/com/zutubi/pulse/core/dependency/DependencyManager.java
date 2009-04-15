package com.zutubi.pulse.core.dependency;

import java.util.List;

/**
 * The base class for managing the integrated dependency system within Pulse.
 */
public interface DependencyManager
{
    /**
     * Get the list of available build statuses.
     *
     * @return the list of build statuses.
     */
    List<String> getStatuses();

    /**
     * Get the default build status
     *
     * @return the default build status.
     */
    String getDefaultStatus();
}
