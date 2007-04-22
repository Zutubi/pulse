package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.model.persistence.BuildSpecificationDao;
import com.zutubi.pulse.model.persistence.ProjectDao;

import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

/**
 * 
 *
 */
public class HibernateProjectDaoTest extends MasterPersistenceTestCase
{
    private ProjectDao projectDao;
    private BuildSpecificationDao buildSpecificationDao;

    public void setUp() throws Exception
    {
        super.setUp();
        projectDao = (ProjectDao) context.getBean("projectDao");
        buildSpecificationDao = (BuildSpecificationDao) context.getBean("buildSpecificationDao");
    }

    public void tearDown() throws Exception
    {
        projectDao = null;
        buildSpecificationDao = null;
        super.tearDown();
    }

    public void testLoadSave()
    {
        Project project = new Project("test-project", "This is a test project");

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
        details.setEnvironment(environment);

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

        MakePulseFileDetails details = new MakePulseFileDetails("Makefile", "build test", "arg1", "workdir");
        details.setEnvironment(environment);
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

    public void testLoadSaveTagAction()
    {
        BuildSpecification spec = new BuildSpecification("test");
        buildSpecificationDao.save(spec);

        TagPostBuildAction action = new TagPostBuildAction();
        action.setSpecifications(Arrays.asList(spec));
        action.setStates(Arrays.asList(ResultState.SUCCESS));
        action.setTag("tag-name");
        action.setMoveExisting(true);
        projectDao.save(action);
        commitAndRefreshTransaction();

        TagPostBuildAction otherAction = projectDao.findTagPostBuildAction(action.getId());
        assertPropertyEquals(action, otherAction);
    }

    public void testLoadSaveExecutableAction()
    {
        BuildSpecification spec = new BuildSpecification("test");
        buildSpecificationDao.save(spec);

        RunExecutablePostBuildAction action = new RunExecutablePostBuildAction();
        action.setSpecifications(Arrays.asList(spec));
        action.setStates(Arrays.asList(ResultState.SUCCESS));
        action.setCommand("command");
        action.setArguments("args");
        projectDao.save(action);
        commitAndRefreshTransaction();

        RunExecutablePostBuildAction otherAction = projectDao.findRunExecutablePostBuildAction(action.getId());
        assertPropertyEquals(action, otherAction);
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

    public void testFindByBuildSpecification()
    {
        Project p1 = new Project("p1", "This is a test project");
        Project p2 = new Project("p2", "This is a test project");
        BuildSpecification spec1 = new BuildSpecification();
        BuildSpecification spec2 = new BuildSpecification();
        p1.addBuildSpecification(spec1);
        p2.addBuildSpecification(spec2);
        projectDao.save(p1);
        projectDao.save(p2);
        commitAndRefreshTransaction();

        // assert that we get the right project back.
        Project found = projectDao.findByBuildSpecification(spec1);
        assertNotNull(found);
        assertEquals(p1.getId(), found.getId());
    }

    public void testFindByCleanupRule()
    {
        Project p1 = new Project("p1", "This is a test project");
        Project p2 = new Project("p2", "This is a test project");
        CleanupRule r1 = new CleanupRule(true, null, 5, CleanupRule.CleanupUnit.BUILDS);
        CleanupRule r2 = new CleanupRule(true, null, 5, CleanupRule.CleanupUnit.BUILDS);
        p1.addCleanupRule(r1);
        p2.addCleanupRule(r2);
        projectDao.save(p1);
        projectDao.save(p2);
        commitAndRefreshTransaction();

        Project found = projectDao.findByCleanupRule(r1);
        assertNotNull(found);
        assertEquals(p1.getId(), found.getId());
    }

    public void testFindByAdminAuthority()
    {
        addAdminProject("justA1");
        addAdminProject("justA1", "A1");
        addAdminProject("justA2", "A2");
        addAdminProject("bothA1A2", "A1", "A2");

        commitAndRefreshTransaction();
        List<Project> projects = projectDao.findByAdminAuthority("A1");
        assertEquals(2, projects.size());
        assertEquals("justA1", projects.get(0).getName());
        assertEquals("bothA1A2", projects.get(1).getName());
    }

    private Project addAdminProject(String name, String... authorities)
    {
        Project p = new Project(name, "test");
        for(String a: authorities)
        {
            p.addAdmin(a);
        }
        projectDao.save(p);
        return p;
    }
}

