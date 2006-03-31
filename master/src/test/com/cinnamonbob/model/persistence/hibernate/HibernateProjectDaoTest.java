package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.core.model.ResultState;
import com.cinnamonbob.model.*;
import com.cinnamonbob.model.persistence.ContactPointDao;
import com.cinnamonbob.model.persistence.ProjectDao;
import com.cinnamonbob.model.persistence.SubscriptionDao;

import java.util.TreeMap;

/**
 * 
 *
 */
public class HibernateProjectDaoTest extends MasterPersistenceTestCase
{
    private ProjectDao projectDao;
    private SubscriptionDao subscriptionDao;
    private ContactPointDao contactDao;

    public void setUp() throws Exception
    {
        super.setUp();
        projectDao = (ProjectDao) context.getBean("projectDao");
        subscriptionDao = (SubscriptionDao) context.getBean("subscriptionDao");
        contactDao = (ContactPointDao) context.getBean("contactPointDao");
    }

    public void tearDown() throws Exception
    {
        projectDao = null;
        subscriptionDao = null;
        contactDao = null;
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

    public void testLoadSaveCleanupRule()
    {
        cleanupRuleHelper(new CleanupRule(true, new ResultState[] { ResultState.ERROR, ResultState.FAILURE }, 5, CleanupRule.CleanupUnit.BUILDS));
    }

    public void testLoadSaveCleanupRuleNoStates()
    {
        cleanupRuleHelper(new CleanupRule(true, null, 5, CleanupRule.CleanupUnit.BUILDS));
    }

    public void testLoadSaveCleanupRuleDays()
    {
        cleanupRuleHelper(new CleanupRule(false, new ResultState[] { ResultState.SUCCESS }, 11, CleanupRule.CleanupUnit.DAYS));
    }

    private void cleanupRuleHelper(CleanupRule rule)
    {
        Project project = new Project("yay", "test");
        project.addCleanupRule(rule);

        projectDao.save(project);
        commitAndRefreshTransaction();

        Project otherProject = projectDao.findById(project.getId());
        CleanupRule otherRule = otherProject.getCleanupRule(rule.getId());
        assertEquals(rule, otherRule);
    }

    public void testLoadSaveCustomBobFileSource()
    {
        CustomBobFileDetails details = new CustomBobFileDetails("hello");

        projectDao.save(details);
        commitAndRefreshTransaction();

        CustomBobFileDetails otherDetails = projectDao.findCustomBobFileSource(details.getId());
        assertPropertyEquals(details, otherDetails);
    }

    public void testLoadSaveAntBobFileDetails()
    {
        TreeMap<String, String> environment = new TreeMap<String, String>();
        environment.put("PATH", "/bin");

        AntBobFileDetails details = new AntBobFileDetails("build.xml", "build test", "arg1", "workdir", environment);

        projectDao.save(details);
        commitAndRefreshTransaction();

        AntBobFileDetails otherDetails = projectDao.findAntBobFileSource(details.getId());
        assertEquals(details.getBuildFile(), otherDetails.getBuildFile());
        assertEquals(details.getTargets(), otherDetails.getTargets());
        assertEquals(details.getArguments(), otherDetails.getArguments());
        assertEquals(details.getWorkingDir(), otherDetails.getWorkingDir());
        for (String key : environment.keySet())
        {
            assertEquals(environment.get(key), otherDetails.getEnvironment().get(key));
        }
    }

    public void testLoadSaveMakeBobFileDetails()
    {
        TreeMap<String, String> environment = new TreeMap<String, String>();
        environment.put("PATH", "/bin");

        MakeBobFileDetails details = new MakeBobFileDetails("Makefile", "build test", "arg1", "workdir", environment);

        projectDao.save(details);
        commitAndRefreshTransaction();

        MakeBobFileDetails otherDetails = projectDao.findMakeBobFileSource(details.getId());
        assertEquals(details.getMakefile(), otherDetails.getMakefile());
        assertEquals(details.getTargets(), otherDetails.getTargets());
        assertEquals(details.getArguments(), otherDetails.getArguments());
        assertEquals(details.getWorkingDir(), otherDetails.getWorkingDir());
        for (String key : environment.keySet())
        {
            assertEquals(environment.get(key), otherDetails.getEnvironment().get(key));
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

