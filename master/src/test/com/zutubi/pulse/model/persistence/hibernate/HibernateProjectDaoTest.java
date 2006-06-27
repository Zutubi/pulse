package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.model.persistence.ContactPointDao;
import com.zutubi.pulse.model.persistence.ProjectDao;
import com.zutubi.pulse.model.persistence.SubscriptionDao;

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

    public void testLoadSaveVersionedPulseFileSource()
    {
        VersionedPulseFileDetails details = new VersionedPulseFileDetails("hello");

        projectDao.save(details);
        commitAndRefreshTransaction();

        VersionedPulseFileDetails otherDetails = projectDao.findVersionedPulseFileDetails(details.getId());
        assertPropertyEquals(details, otherDetails);
    }

    public void testLoadSaveAntPulseFileDetails()
    {
        TreeMap<String, String> environment = new TreeMap<String, String>();
        environment.put("PATH", "/bin");

        AntPulseFileDetails details = new AntPulseFileDetails("build.xml", "build test", "arg1", "workdir", environment);

        projectDao.save(details);
        commitAndRefreshTransaction();

        AntPulseFileDetails otherDetails = projectDao.findAntPulseFileSource(details.getId());
        assertEquals(details.getBuildFile(), otherDetails.getBuildFile());
        assertEquals(details.getTargets(), otherDetails.getTargets());
        assertEquals(details.getArguments(), otherDetails.getArguments());
        assertEquals(details.getWorkingDir(), otherDetails.getWorkingDir());
        for (String key : environment.keySet())
        {
            assertEquals(environment.get(key), otherDetails.getEnvironment().get(key));
        }
    }

    public void testLoadSaveMakePulseFileDetails()
    {
        TreeMap<String, String> environment = new TreeMap<String, String>();
        environment.put("PATH", "/bin");

        MakePulseFileDetails details = new MakePulseFileDetails("Makefile", "build test", "arg1", "workdir", environment);

        projectDao.save(details);
        commitAndRefreshTransaction();

        MakePulseFileDetails otherDetails = projectDao.findMakePulseFileSource(details.getId());
        assertEquals(details.getMakefile(), otherDetails.getMakefile());
        assertEquals(details.getTargets(), otherDetails.getTargets());
        assertEquals(details.getArguments(), otherDetails.getArguments());
        assertEquals(details.getWorkingDir(), otherDetails.getWorkingDir());
        for (String key : environment.keySet())
        {
            assertEquals(environment.get(key), otherDetails.getEnvironment().get(key));
        }
    }

    public void testLoadSaveFileCapture()
    {
        AntPulseFileDetails details = new AntPulseFileDetails();
        FileCapture capture = new FileCapture("name", "file", "type");
        capture.addProcessor("processor");
        details.addCapture(capture);

        projectDao.save(details);
        commitAndRefreshTransaction();

        AntPulseFileDetails otherDetails = projectDao.findAntPulseFileSource(details.getId());
        assertPropertyEquals(capture, otherDetails.getCaptures().get(0));
    }

    public void testLoadSaveDirectoryCapture()
    {
        AntPulseFileDetails details = new AntPulseFileDetails();
        DirectoryCapture capture = new DirectoryCapture("name", "file", "type");
        capture.setIncludes("include pattern");
        capture.setIncludes("exclude pattern");
        capture.addProcessor("processor");
        details.addCapture(capture);

        projectDao.save(details);
        commitAndRefreshTransaction();

        AntPulseFileDetails otherDetails = projectDao.findAntPulseFileSource(details.getId());
        assertPropertyEquals(capture, otherDetails.getCaptures().get(0));
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

