package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.ProjectGroup;
import com.zutubi.pulse.model.persistence.ProjectDao;
import com.zutubi.pulse.model.persistence.ProjectGroupDao;

/**
 *
 *
 */
public class HibernateProjectGroupDaoTest extends MasterPersistenceTestCase
{
    private ProjectDao projectDao;
    private ProjectGroupDao projectGroupDao;

    public void setUp() throws Exception
    {
        super.setUp();
        projectDao = (ProjectDao) context.getBean("projectDao");
        projectGroupDao = (ProjectGroupDao) context.getBean("projectGroupDao");
    }

    public void tearDown() throws Exception
    {
        projectDao = null;
        projectGroupDao = null;
        super.tearDown();
    }

    public void testLoadSave()
    {
        Project project = new Project();
        projectDao.save(project);

        ProjectGroup group = new ProjectGroup("my group");
        group.add(project);
        projectGroupDao.save(group);

        commitAndRefreshTransaction();

        ProjectGroup otherGroup = projectGroupDao.findById(group.getId());
        assertPropertyEquals(group, otherGroup);
        assertEquals(1, otherGroup.getProjects().size());
        assertEquals(project.getId(), otherGroup.getProjects().get(0).getId());
    }

    public void testFindByName()
    {
        ProjectGroup g1 = new ProjectGroup("g1");
        ProjectGroup g2 = new ProjectGroup("g2");
        projectGroupDao.save(g1);
        projectGroupDao.save(g2);

        commitAndRefreshTransaction();

        ProjectGroup found = projectGroupDao.findByName("g1");
        assertEquals(g1.getId(), found.getId());

        assertNull(projectGroupDao.findByName("none"));
    }

    public void testFindByProject()
    {
        Project project = new Project();
        projectDao.save(project);

        ProjectGroup g1 = new ProjectGroup("g1");
        g1.add(project);
        projectGroupDao.save(g1);
        
        ProjectGroup g2 = new ProjectGroup("g2");
        g2.add(project);
        projectGroupDao.save(g2);

        commitAndRefreshTransaction();

        assertEquals(2, projectGroupDao.findByProject(project).size());

        g2 = projectGroupDao.findByName("g2");
        g2.remove(project);
        commitAndRefreshTransaction();

        assertEquals(1, projectGroupDao.findByProject(project).size());

        g1 = projectGroupDao.findByName("g1");
        g1.remove(project);
        commitAndRefreshTransaction();

        assertEquals(0, projectGroupDao.findByProject(project).size());
    }
}
