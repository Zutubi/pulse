package com.zutubi.pulse.master.model.persistence.hibernate;

import com.zutubi.pulse.master.model.persistence.TriggerDao;
import com.zutubi.pulse.master.scheduling.Trigger;

import java.util.List;

/**
 * Hibernate implementation of {@link TriggerDao}.
 */
public class HibernateTriggerDao extends HibernateEntityDao<Trigger> implements TriggerDao
{
    public Class<Trigger> persistentClass()
    {
        return Trigger.class;
    }

    public List<Trigger> findByGroup(String group)
    {
        return findByNamedQuery("findByGroup", "group", group);
    }

    public List<Trigger> findByProject(long id)
    {
        return findByNamedQuery("findByProject", "project", id);
    }

    public Trigger findByProjectAndName(long id, String name)
    {
        return (Trigger) findUniqueByNamedQuery("findByProjectAndName", "project", id, "name", name);
    }

    public Trigger findByNameAndGroup(String name, String group)
    {
        return (Trigger) findUniqueByNamedQuery("findByNameAndGroup", "name", name, "group", group);
    }
}
