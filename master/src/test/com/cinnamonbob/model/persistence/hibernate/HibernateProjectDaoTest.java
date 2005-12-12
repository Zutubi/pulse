package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.model.persistence.ProjectDao;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.Svn;

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

        projectDao.save(project);

        commitAndRefreshTransaction();

        Project otherProject = projectDao.findById(project.getId());
        assertPropertyEquals(project, otherProject);
    }

    public void testFindByLikeName()
    {
        Project projectA = new Project("nameA", "description");
        Project projectB = new Project("nameB", "description");
        projectDao.save(projectA);
        projectDao.save(projectB);
        commitAndRefreshTransaction();
        assertEquals(2, projectDao.findByLikeName("%name%").size());
        assertEquals(1, projectDao.findByLikeName("%A%").size());
    }

    public void testFindByName()
    {
        Project projectA = new Project("nameA", "description");
        projectDao.save(projectA);
        commitAndRefreshTransaction();
        assertNotNull(projectDao.findByName("nameA"));
    }

}

