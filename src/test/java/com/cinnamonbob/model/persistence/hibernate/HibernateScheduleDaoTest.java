package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.model.*;
import com.cinnamonbob.model.BuildTask;
import com.cinnamonbob.model.persistence.ProjectDao;
import com.cinnamonbob.model.persistence.ScheduleDao;

import java.util.List;


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
        
        Trigger trigger = new CronTrigger("0 0 12 * * ?");
        Task task = new BuildTask();
        Schedule schedule = new Schedule("test", project, task, trigger);

        scheduleDao.save(schedule);
        commitAndRefreshTransaction();

        Schedule anotherSchedule = scheduleDao.findById(schedule.getId());

        // want to ensure that a new object has been created by hibernate and
        // that the old one is not just returned to us.
        assertFalse(schedule == anotherSchedule);
        assertEquals(schedule.getName(), anotherSchedule.getName());
        assertEquals(schedule.getProject(), anotherSchedule.getProject());
        assertNotNull(anotherSchedule.getTrigger());
        assertNotNull(anotherSchedule.getTask());
    }

    public void testFindByProject()
    {
        Project project = new Project();
        projectDao.save(project);

        Trigger trigger = new CronTrigger("0 0 12 * * ?");
        Task task = new BuildTask();
        Schedule schedule = new Schedule("test", project, task, trigger);

        scheduleDao.save(schedule);
        commitAndRefreshTransaction();

        List<Schedule> schedules = scheduleDao.findByProject(project);
        assertEquals(1, schedules.size());
        assertEquals(schedule, schedules.get(0));
    }
}
