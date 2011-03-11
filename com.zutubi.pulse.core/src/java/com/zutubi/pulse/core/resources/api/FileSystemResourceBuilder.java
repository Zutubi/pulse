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
     * Used to separate elements of resource property names.
     */
    public static final String PROPERTY_SEPARATOR                = ".";
    /**
     * Suffix for properties relating to binaries or directories that contain
     * them.
     */
    public static final String PROPERTY_SUFFIX_BINARY            = "bin";
    /**
     * Suffix used for properties that point to directories.
     */
    public static final String PROPERTY_SUFFIX_DIRECTORY         = "dir";
    /**
     * Suffix for properties relating to libararies or directories that contain
     * them.
     */
    public static final String PROPERTY_SUFFIX_LIBRARY           = "lib";
    /**
     * Standard suffix for properties that point to directories of binaries.
     */
    public static final String PROPERTY_SUFFIX_BINARY_DIRECTORY  = PROPERTY_SUFFIX_BINARY + PROPERTY_SEPARATOR + PROPERTY_SUFFIX_DIRECTORY;
    /**
     * Standard suffix for properties that point to directories of libraries.
     */
    public static final String PROPERTY_SUFFIX_LIBRARY_DIRECTORY = PROPERTY_SUFFIX_LIBRARY + PROPERTY_SEPARATOR + PROPERTY_SUFFIX_DIRECTORY;

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
