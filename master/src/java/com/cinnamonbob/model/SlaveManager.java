package com.cinnamonbob.model;

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
