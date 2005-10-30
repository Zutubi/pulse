package com.cinnamonbob.schedule.persistence.hibernate;

import com.cinnamonbob.model.persistence.hibernate.HibernateEntityDao;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.schedule.Schedule;
import com.cinnamonbob.schedule.Trigger;
import com.cinnamonbob.schedule.persistence.ScheduleDao;

import java.util.List;
import java.util.Collections;

/**
 * <class-comment/>
 */
public class HibernateScheduleDao extends HibernateEntityDao<Schedule> implements ScheduleDao
{
    public Class persistentClass()
    {
        return Schedule.class;
    }

    public Schedule findBy(Project project, String name)
    {
        return (Schedule) findUniqueByNamedQuery("schedule.findByNameAndProject", "project", project, "name", name);
    }

    public List<Schedule> findByProject(Project project)
    {
        return findByNamedQuery("schedule.findByProject", "project", project);
    }

    public List<Trigger> findAllTriggers()
    {
        return (List<Trigger>)getHibernateTemplate().find("from " + Trigger.class.getName());
    }

    public Schedule findByTrigger(long id)
    {
        return (Schedule) findUniqueByNamedQuery("schedule.findByTrigger", "id", id);
    }
}