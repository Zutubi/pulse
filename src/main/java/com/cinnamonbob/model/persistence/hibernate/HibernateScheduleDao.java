package com.cinnamonbob.model.persistence.hibernate;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.Schedule;
import com.cinnamonbob.model.Subscription;
import com.cinnamonbob.model.persistence.ScheduleDao;

/**
 * 
 *
 */
public class HibernateScheduleDao extends HibernateEntityDao<Schedule> implements ScheduleDao
{
    @Override
    public Class persistentClass()
    {
        return Schedule.class;
    }

    public List<Schedule> findByProject(final Project project)
    {
        return (List<Schedule>)findByNamedQuery("schedule.findByProject", "project", project);
    }

}
