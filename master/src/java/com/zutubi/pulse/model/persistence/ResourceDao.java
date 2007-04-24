package com.zutubi.pulse.model.persistence;

import com.zutubi.pulse.model.Slave;
import com.zutubi.pulse.model.PersistentResource;

import java.util.List;

/**
 */
public interface ResourceDao extends EntityDao
{
    List<PersistentResource> findAllBySlave(Slave slave);
    PersistentResource findBySlaveAndName(Slave slave, String name);
}
