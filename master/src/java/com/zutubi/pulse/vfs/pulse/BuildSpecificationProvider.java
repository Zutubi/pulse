package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.model.BuildSpecification;
import org.apache.commons.vfs.FileSystemException;

/**
 * <class comment/>
 */
public interface BuildSpecificationProvider
{
    BuildSpecification getBuildSpecification() throws FileSystemException;

    long getBuildSpecificationId() throws FileSystemException;
}
