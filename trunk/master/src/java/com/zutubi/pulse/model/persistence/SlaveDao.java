package com.zutubi.pulse.model.persistence;

import com.zutubi.pulse.model.Slave;

/**
 */
public interface SlaveDao extends EntityDao<Slave>
{
    Slave findByName(String name);
}
