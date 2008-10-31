package com.zutubi.pulse.master.model.persistence.hibernate;

import com.zutubi.pulse.master.model.persistence.TriggerDao;
import com.zutubi.pulse.master.scheduling.Trigger;

import java.util.List;

/**
 * <class-comment/>
 */
public class HibernateTriggerDao extends HibernateEntityDao<Trigger> implements TriggerDao
{
    public Class persistentClass()
    {
        return Trigger.class;
    }

    public List<Trigger> findByGroup(String group)
    {
        return findByNamedQuery("findByGroup", "group", group);
    }

    public List<Trigger> findByProject(long id)
    {
        return findByNamedQuery("findByProject", "project", Long.valueOf(id));
    }

    public Trigger findByProjectAndName(long id, String name)
    {
        return (Trigger) findUniqueByNamedQuery("findByProjectAndName", "project", Long.valueOf(id), "name", name);
    }

    public Trigger findByNameAndGroup(String name, String group)
    {
        return (Trigger) findUniqueByNamedQuery("findByNameAndGroup", "name", name, "group", group);
    }
}
