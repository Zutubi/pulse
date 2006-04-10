package com.zutubi.pulse.model.persistence;

import com.zutubi.pulse.model.BuildSpecification;

/**
 * <class-comment/>
 */
public interface BuildSpecificationDao extends EntityDao<BuildSpecification>
{
    BuildSpecification findByName(String name);
}
