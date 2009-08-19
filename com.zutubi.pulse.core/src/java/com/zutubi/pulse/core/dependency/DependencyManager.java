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

    /**
     * Get the priority of the status.  The priority represents an ordering that allows interaction
     * between the different status's.  Take 'integration' and 'release' for example.  'release' has a
     * priority of 1, 'integration' or 3.  The priority of 3 for integration means that it will accept
     * any artifact of a status lower than or equal to itself.  So, an integration build will happily
     * accept release builds.  However, a release build will not accept an integration build.
     *
     * @param status    the status string.
     * @return  a number that represents priority of the status, as explained above.
     */
    int getPriority(String status);
}
