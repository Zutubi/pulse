package com.cinnamonbob.model.persistence;

import com.cinnamonbob.core.model.Resource;

/**
 */
public interface ResourceDao extends EntityDao<Resource>
{
    Resource findByName(String name);
}
