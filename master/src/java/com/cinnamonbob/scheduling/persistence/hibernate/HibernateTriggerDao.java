package com.cinnamonbob.scheduling.persistence.hibernate;

import com.cinnamonbob.model.persistence.hibernate.HibernateEntityDao;
import com.cinnamonbob.scheduling.Trigger;
import com.cinnamonbob.scheduling.persistence.TriggerDao;

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

    public Trigger findByNameAndGroup(String name, String group)
    {
        return (Trigger) findUniqueByNamedQuery("findByNameAndGroup", "name", name, "group", group);
    }
}
