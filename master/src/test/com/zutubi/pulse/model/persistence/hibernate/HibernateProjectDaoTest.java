package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.persistence.ProjectDao;

import java.util.List;

/**
 * 
 *
 */
public class HibernateProjectDaoTest extends MasterPersistenceTestCase
{
    private ProjectDao projectDao;

    public void setUp() throws Exception
    {
        super.setUp();
        projectDao = (ProjectDao) context.getBean("projectDao");
    }

    public void tearDown() throws Exception
    {
        projectDao = null;
        super.tearDown();
    }

    public void testLoadSave()
    {
        Project project = new Project();
        project.setLastPollTime((long)33442);
        project.pause();

        projectDao.save(project);

        commitAndRefreshTransaction();

        Project otherProject = projectDao.findById(project.getId());
        assertPropertyEquals(project, otherProject);
    }

    public void testFindByAdminAuthority()
    {
        Project a = addAdminProject("A1");
        addAdminProject("A2");
        Project d = addAdminProject("A1", "A2");

        commitAndRefreshTransaction();
        List<Project> projects = projectDao.findByAdminAuthority("A1");
        assertEquals(2, projects.size());
        assertEquals(a, projects.get(0));
        assertEquals(d, projects.get(1));
    }

    private Project addAdminProject(String... authorities)
    {
        Project p = new Project();
        for(String a: authorities)
        {
            p.addAdmin(a);
        }
        projectDao.save(p);
        return p;
    }
}

