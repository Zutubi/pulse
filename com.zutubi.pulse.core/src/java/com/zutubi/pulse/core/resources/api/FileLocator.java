package com.zutubi.pulse.core.resources.api;

import java.io.File;
import java.util.Collection;

/**
 * Interface for classes that can locate paths of interest on the file system.
 */
public interface FileLocator
{
    /**
     * Locates and returns all paths of interest.
     * 
     * @return all located files
     */
    Collection<File> locate();
}
