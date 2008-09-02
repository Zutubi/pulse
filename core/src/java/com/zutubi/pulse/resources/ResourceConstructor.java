package com.zutubi.pulse.resources;

import com.zutubi.pulse.core.config.Resource;

import java.io.File;
import java.io.IOException;

/**
 * The resource constructor implements the smarts for a understanding a particular type of
 * resource.
 *
 * A resource is currently identified as having a home (installation) directory, along with
 * a set of resource specific properties.
 *
 * Resources are *required* to have a home.
 */
public interface ResourceConstructor
{
    /**
     * Return the path for the resource home directory if it exists.
     *
     * @return path
     */
    String lookupHome();

    /**
     * Return true if the given string represents a home directory for this resource.
     *
     * @param home path
     *
     * @return true if the specified path represents a home directory
     */
    boolean isResourceHome(String home);

    /**
     * Return true if the given file represents a home directory for this resource.
     *
     * @param home path
     *
     * @return true if the specified path represents a home directory
     */
    boolean isResourceHome(File home);

    /**
     * Create a new resource instance configured with the given home path.
     *
     * @param home path for which isResourceHome returns true.
     *
     * @return a configured resource instance
     *
     * @throws IOException if an error occurs.
     */
    Resource createResource(String home) throws IOException;

    /**
     * Create a new resource instance configured with the given home file.
     *
     * @param home path for which isResourceHome returns true.
     *
     * @return a configured resource instance
     *
     * @throws IOException if an error occurs.
     */
    Resource createResource(File home) throws IOException;
}
