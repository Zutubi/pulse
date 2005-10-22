package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.model.persistence.ProjectDao;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.Schedule;
import com.cinnamonbob.model.Svn;

import java.util.List;

/**
 * 
 *
 */
public class HibernateProjectDaoTest extends PersistenceTestCase
{
    private ProjectDao projectDao;

    public void setUp() throws Exception
    {
        super.setUp();
        projectDao = (ProjectDao) context.getBean("projectDao");
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testLoadSave()
    {
        Project project = new Project("test-project", "This is a test project");
        
        Svn svn = new Svn();
        project.addScm(svn);
        svn.setProject(project);
        
//        Schedule schedule = new Schedule("schedule", project, "recipe");
//        project.addSchedule(schedule);
        projectDao.save(project);

        commitAndRefreshTransaction();

        Project otherProject = (Project) projectDao.findById(project.getId());
        assertEquals(project.getName(), otherProject.getName());
        assertEquals(project.getDescription(), otherProject.getDescription());
        assertEquals(project.getScms(), otherProject.getScms());
        assertEquals(project.getSchedules(), otherProject.getSchedules());
    }

    public void testFindByName()
    {
        Project project = new Project("testName", "Description");
        projectDao.save(project);
        commitAndRefreshTransaction();

        assertNull(projectDao.findByName("someName"));
        assertNotNull(projectDao.findByName("testName"));
    }

    public void testFindByLikeName()
    {
        Project project = new Project("aName", "description");
        projectDao.save(project);
        commitAndRefreshTransaction();

        assertEquals(0, projectDao.findByLikeName("b").size());
        assertEquals(1, projectDao.findByLikeName("%a%").size());
        assertEquals(1, projectDao.findByLikeName("%Name%").size());
        assertEquals(1, projectDao.findByLikeName("%N%").size());
    }
}

