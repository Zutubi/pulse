package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.persistence.ProjectDao;

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
}

