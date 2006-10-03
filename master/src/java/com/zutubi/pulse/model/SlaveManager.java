package com.zutubi.pulse.model;

import com.zutubi.pulse.license.LicenseException;

import java.util.List;

/**
 * <class-comment/>
 */
public interface SlaveManager extends EntityManager<Slave>
{
    Slave getSlave(String name);

    List<Slave> getAll();

    Slave getSlave(long id);

    void delete(long id);

}
