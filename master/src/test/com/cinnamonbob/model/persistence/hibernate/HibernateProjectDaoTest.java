package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.core.model.AntBobFileDetails;
import com.cinnamonbob.core.model.CustomBobFileDetails;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.Svn;
import com.cinnamonbob.model.persistence.ProjectDao;

import java.util.TreeMap;

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
        project.setScm(svn);

        projectDao.save(project);

        commitAndRefreshTransaction();

        Project otherProject = projectDao.findById(project.getId());
        assertPropertyEquals(project, otherProject);
    }

    public void testLoadSaveCustomBobFileSource()
    {
        CustomBobFileDetails details = new CustomBobFileDetails("hello");

        projectDao.save(details);
        commitAndRefreshTransaction();

        CustomBobFileDetails otherDetails = projectDao.findCustomBobFileSource(details.getId());
        assertPropertyEquals(details, otherDetails);
    }

    public void testLoadSaveAntBobFileSource()
    {
        TreeMap<String, String> environment = new TreeMap<String, String>();
        environment.put("PATH", "/bin");

        AntBobFileDetails source = new AntBobFileDetails("build.xml", "build test", environment);

        projectDao.save(source);
        commitAndRefreshTransaction();

        AntBobFileDetails otherSource = projectDao.findAntBobFileSource(source.getId());
        assertPropertyEquals(source, otherSource);
        for (String key : environment.keySet())
        {
            assertEquals(environment.get(key), otherSource.getEnvironment().get(key));
        }
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

