package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.model.BuildSpecification;

/**
 * <class comment/>
 */
public interface BuildSpecificationProvider
{
    BuildSpecification getBuildSpecification();

    long getBuildSpecificationId();
}
