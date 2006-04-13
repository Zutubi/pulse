/********************************************************************************
  @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.model.persistence;

import com.zutubi.pulse.core.model.Resource;

/**
 */
public interface ResourceDao extends EntityDao<Resource>
{
    Resource findByName(String name);
}
