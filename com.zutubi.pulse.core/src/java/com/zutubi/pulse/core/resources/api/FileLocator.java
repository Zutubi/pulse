package com.zutubi.pulse.core.resources.api;

import java.io.File;
import java.util.List;

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
    List<File> locate();
}
