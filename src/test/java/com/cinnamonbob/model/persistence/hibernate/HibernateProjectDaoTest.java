package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.model.persistence.ProjectDao;
import com.cinnamonbob.model.Project;
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
        projectDao.save(project);

        commitAndRefreshTransaction();

        Project otherProject = (Project) projectDao.findById(project.getId());
        assertEquals(project.getName(), otherProject.getName());
        assertEquals(project.getDescription(), otherProject.getDescription());
        assertEquals(project.getScms(), otherProject.getScms());
    }

    private void assertEquals(List a, List b)
    {
        assertEquals(a.size(), b.size());
        for (int i = 0; i < a.size(); i++)
        {
            assertEquals(a.get(i), b.get(i));
        }
    }
}

