package com.cinnamonbob.scheduling.persistence.hibernate;

import com.cinnamonbob.scheduling.Task;
import com.cinnamonbob.scheduling.Trigger;
import com.cinnamonbob.scheduling.persistence.TaskDao;
import com.cinnamonbob.model.persistence.hibernate.HibernateEntityDao;

import java.util.List;

/**
 * <class-comment/>
 */
public class HibernateTaskDao extends HibernateEntityDao<Task> implements TaskDao
{
    public Class persistentClass()
    {
        return Task.class;
    }

    public List<Task> findByGroup(String group)
    {
        return findByNamedQuery("findByGroup", "group", group);
    }

    public Task findByNameAndGroup(String name, String group)
    {
        return (Task) findUniqueByNamedQuery("findByNameAndGroup", "name", name, "group", group);
    }
}
