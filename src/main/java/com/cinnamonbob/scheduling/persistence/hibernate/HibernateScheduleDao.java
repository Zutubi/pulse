package com.cinnamonbob.scheduling.persistence.hibernate;

import com.cinnamonbob.model.persistence.hibernate.HibernateEntityDao;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.scheduling.persistence.ScheduleDao;
import com.cinnamonbob.scheduling.Schedule;

import java.util.List;

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
}
