package com.zutubi.pulse.master.model.persistence;

import com.zutubi.pulse.scheduling.Trigger;

import java.util.List;

/**
 * <class-comment/>
 */
public interface TriggerDao extends EntityDao<Trigger>
{
    Trigger findByNameAndGroup(String name, String group);

    List<Trigger> findByGroup(String group);

    List<Trigger> findByProject(long id);

    Trigger findByProjectAndName(long id, String name);
}
