package com.zutubi.pulse.core.resources.api;

import java.io.File;

/**
 * Interface for the creation of resources from a file system path.  In a
 * typical example the path is the root of an installation directory, and the
 * built resource contains properties pointing to files of interest (binaries,
 * library directories, etc) within it.
 * <p/>
 * Rather than implementing this class, consider using or extending the
 * existing implementations which cover common cases.
 */
public interface FileSystemResourceBuilder
{
    /**
     * Suffix for properties that point to binaries.
     */
    public static final String PROPERTY_SUFFIX_BINARY            = ".bin";
    /**
     * Standard suffix for properties that point to directories of binaries.
     */
    public static final String PROPERTY_SUFFIX_BINARY_DIRECTORY  = ".bin.dir";
    /**
     * Standard suffix for properties that point to directories of libraries.
     */
    public static final String PROPERTY_SUFFIX_LIBRARY_DIRECTORY = ".lib.dir";

    /**
     * Builds a resource representing the tool found at the given path, if any.
     * If the resource cannot be created (e.g. the tool is not found at the
     * path after all), null should be returned.
     * 
     * @param path located path of the resource on the file system
     * @return a resource configuration for the tool at the path, or null if no
     *         such resource may be sensibly created
     */
    ResourceConfiguration buildResource(File path);
}
