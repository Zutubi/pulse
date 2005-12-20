package com.cinnamonbob.model.persistence;

import com.cinnamonbob.model.BuildSpecification;

/**
 * <class-comment/>
 */
public interface BuildSpecificationDao extends EntityDao<BuildSpecification>
{
    BuildSpecification findByName(String name);
}
