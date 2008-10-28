package com.zutubi.pulse.master.model.persistence.hibernate;

import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.persistence.ProjectDao;

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

        projectDao.save(project);

        commitAndRefreshTransaction();

        Project otherProject = projectDao.findById(project.getId());
        assertPropertyEquals(project, otherProject);
    }
}

