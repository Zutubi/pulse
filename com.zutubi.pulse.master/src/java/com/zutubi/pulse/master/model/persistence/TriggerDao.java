package com.zutubi.pulse.master.model.persistence;

import com.zutubi.pulse.master.scheduling.Trigger;

import java.util.List;

/**
 * Provides access to trigger entities.
 */
public interface TriggerDao extends EntityDao<Trigger>
{
    Trigger findByNameAndGroup(String name, String group);

    List<Trigger> findByGroup(String group);

    List<Trigger> findByProject(long id);

    Trigger findByProjectAndName(long id, String name);
}
