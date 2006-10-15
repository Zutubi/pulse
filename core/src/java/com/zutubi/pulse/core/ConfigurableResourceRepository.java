package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.Resource;

/**
 * <class comment/>
 */
public interface ConfigurableResourceRepository extends ResourceRepository
{
    void addResource(Resource resource);

    void addResource(Resource resource, boolean overwrite);
}
