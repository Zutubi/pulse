package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.core.FileArtifact;
import com.cinnamonbob.model.*;
import com.cinnamonbob.model.persistence.ProjectDao;
import com.cinnamonbob.model.persistence.ScheduleDao;

import java.io.File;
import java.util.Calendar;


/**
 * 
 *
 */
public class HibernateScheduleDaoTest extends PersistenceTestCase
{
    private ScheduleDao scheduleDao;
    private ProjectDao projectDao;

    public void setUp() throws Exception
    {
        super.setUp();
        scheduleDao = (ScheduleDao) context.getBean("scheduleDao");
        projectDao = (ProjectDao) context.getBean("projectDao");
    }

    public void tearDown() throws Exception
    {
        try
        {
            super.tearDown();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void testSaveAndLoad()
    {
        Project project = new Project();
        projectDao.save(project);
        
        CronTrigger trigger = new CronTrigger("0 0 12 * * ?");
        Schedule schedule = new Schedule("test", project, "recipe");
        schedule.add(trigger);
        
        scheduleDao.save(schedule);
        commitAndRefreshTransaction();

        Schedule anotherSchedule = scheduleDao.findById(schedule.getId());

        // want to ensure that a new object has been created by hibernate and
        // that the old one is not just returned to us.
        assertFalse(schedule == anotherSchedule);
        assertPersistentEquals(schedule, anotherSchedule);
        
    }
}
