package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.model.persistence.*;

import java.util.Calendar;
import java.util.List;


/**
 * See also the BuildQueryTest.
 */
public class HibernateBuildResultDaoTest extends MasterPersistenceTestCase
{
    private static long time = 0;

    private BuildResultDao buildResultDao;
    private ProjectDao projectDao;
    private BuildSpecificationDao buildSpecificationDao;
    private ChangelistDao changelistDao;
    private UserDao userDao;

    public void setUp() throws Exception
    {
        super.setUp();
        buildResultDao = (BuildResultDao) context.getBean("buildResultDao");
        projectDao = (ProjectDao) context.getBean("projectDao");
        buildSpecificationDao = (BuildSpecificationDao) context.getBean("buildSpecificationDao");
        changelistDao = (ChangelistDao) context.getBean("changelistDao");
        userDao = (UserDao) context.getBean("userDao");
    }

    public void tearDown() throws Exception
    {
        buildSpecificationDao = null;
        projectDao = null;
        buildResultDao = null;
        changelistDao = null;
        userDao = null;

        try
        {
            super.tearDown();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void testSaveAndLoadArtifactCommand()
    {
        CommandResult result = createArtifactCommand();
        saveAndLoadCommand(result);
    }

    public void testSaveAndLoadFailedCommand()
    {
        CommandResult result = createFailedCommand();
        saveAndLoadCommand(result);
    }

    public void testSaveAndLoadErroredCommand()
    {
        CommandResult result = createErroredCommand();
        saveAndLoadCommand(result);
    }

    public void testSaveAndLoadRecipe()
    {
        RecipeResult recipe = createRecipe();
        saveAndLoadRecipe(recipe);
    }

    public void testSaveAndLoadErroredRecipe()
    {
        RecipeResult recipe = createErroredRecipe();
        saveAndLoadRecipe(recipe);
    }

    public void testSaveAndLoad()
    {
        RecipeResult recipeResult = createRecipe();

        BuildScmDetails scmDetails = new BuildScmDetails(new NumericalRevision(42));

        Revision revision = new NumericalRevision(12345);
        revision.setDate(Calendar.getInstance().getTime());
        revision.setAuthor("user");
        revision.setComment("i like fruit");

        // Need to save the Project as it is *not* cascaded from BuildResult
        Project project = new Project();
        projectDao.save(project);

        // Ditto for spec
        BuildSpecification spec = new BuildSpecification("specname");
        BuildSpecificationNode node = new BuildSpecificationNode(new BuildStage("name", new AnyCapableBuildHostRequirements(), null));
        spec.getRoot().addChild(node);
        buildSpecificationDao.save(spec);

        BuildResult buildResult = new BuildResult(new TriggerBuildReason("scm trigger"), project, spec, 11, false);
        buildResult.commence();
        buildResult.setScmDetails(scmDetails);
        RecipeResultNode recipeNode = new RecipeResultNode(node.getStage().getPname(), recipeResult);
        recipeNode.setHost("test host");
        buildResult.getRoot().addChild(recipeNode);

        buildResultDao.save(buildResult);
        commitAndRefreshTransaction();

        BuildResult anotherBuildResult = buildResultDao.findById(buildResult.getId());
        assertPropertyEquals(buildResult, anotherBuildResult);
    }

    public void testSaveAndLoadTestSummary()
    {
        TestResultSummary summary = new TestResultSummary(3, 323, 111111);
        RecipeResult result = createRecipe();
        result.setTestSummary(summary);
        saveAndLoadRecipe(result);
    }

    public void testFindResultNodeByResultId()
    {
        PersistentName name = new PersistentName("name");
        buildResultDao.save(name);

        RecipeResult result = createRecipe();
        RecipeResultNode node = new RecipeResultNode(name, result);
        buildResultDao.save(node);
        commitAndRefreshTransaction();

        RecipeResultNode persistentNode = buildResultDao.findResultNodeByResultId(result.getId());
        assertNotNull(persistentNode);
        assertEquals(node.getId(), persistentNode.getId());
    }

    private RecipeResult createRecipe()
    {
        RecipeResult recipeResult = new RecipeResult("project");
        recipeResult.commence();
        recipeResult.complete();

        CommandResult result = createArtifactCommand();
        recipeResult.add(result);
        result = createFailedCommand();
        recipeResult.add(result);
        result = createErroredCommand();
        recipeResult.add(result);
        return recipeResult;
    }

    private RecipeResult createErroredRecipe()
    {
        RecipeResult recipeResult = new RecipeResult("project");
        recipeResult.commence();
        recipeResult.error("Random explosion");
        recipeResult.complete();
        return recipeResult;
    }

    private void saveAndLoadCommand(CommandResult result)
    {
        buildResultDao.save(result);
        commitAndRefreshTransaction();
        CommandResult anotherResult = buildResultDao.findCommandResult(result.getId());
        assertPropertyEquals(result, anotherResult);
    }

    private void saveAndLoadRecipe(RecipeResult result)
    {
        buildResultDao.save(result);
        commitAndRefreshTransaction();
        RecipeResult anotherResult = buildResultDao.findRecipeResult(result.getId());
        assertPropertyEquals(result, anotherResult);
    }

    private CommandResult createErroredCommand()
    {
        CommandResult result;
        result = new CommandResult("command name");
        result.commence();
        result.error("woops!");
        return result;
    }

    private CommandResult createFailedCommand()
    {
        CommandResult result;
        result = new CommandResult("command name");
        result.commence();
        result.failure("oh no!");
        return result;
    }

    private CommandResult createArtifactCommand()
    {
        CommandResult result = new CommandResult("command name");
        result.commence();
        result.success();
        StoredFileArtifact artifact = new StoredFileArtifact("to file");
        PlainFeature feature = new PlainFeature(Feature.Level.ERROR, "getSummary here", 7);

        artifact.addFeature(feature);
        result.addArtifact(new StoredArtifact("test", artifact));
        return result;
    }

    public void testGetOldestBuilds()
    {
        Project p1 = new Project();
        Project p2 = new Project();

        projectDao.save(p1);
        projectDao.save(p2);

        BuildResult r1 = createCompletedBuild(p1, 1);
        BuildResult r2 = createCompletedBuild(p1, 2);
        BuildResult r3 = createCompletedBuild(p1, 3);
        BuildResult r4 = createCompletedBuild(p1, 4);
        BuildResult otherP = createCompletedBuild(p2, 1);

        buildResultDao.save(r1);
        buildResultDao.save(r2);
        buildResultDao.save(r3);
        buildResultDao.save(r4);
        buildResultDao.save(otherP);

        commitAndRefreshTransaction();

        List<BuildResult> oldest = buildResultDao.findOldestByProject(p1, null, 1, false);
        assertEquals(1, oldest.size());
        assertPropertyEquals(r1, oldest.get(0));

        oldest = buildResultDao.findOldestByProject(p1, null, 3, false);
        assertEquals(3, oldest.size());
        assertPropertyEquals(r1, oldest.get(0));
        assertPropertyEquals(r2, oldest.get(1));
        assertPropertyEquals(r3, oldest.get(2));

        oldest = buildResultDao.findOldestByProject(p1, null, 100, false);
        assertEquals(4, oldest.size());
    }

    public void testGetOldestBuildsInitial()
    {
        Project p1 = new Project();
        projectDao.save(p1);

        BuildResult result = new BuildResult(new TriggerBuildReason("scm trigger"), p1, makeSpec(), 1, false);
        buildResultDao.save(result);

        commitAndRefreshTransaction();

        List<BuildResult> oldest = buildResultDao.findOldestByProject(p1, ResultState.getCompletedStates(), 1, false);
        assertEquals(0, oldest.size());
    }

    public void testGetOldestBuildsInProgress()
    {
        Project p1 = new Project();
        projectDao.save(p1);

        BuildResult result = new BuildResult(new TriggerBuildReason("scm trigger"), p1, makeSpec(), 1, false);
        result.commence(0);
        buildResultDao.save(result);

        commitAndRefreshTransaction();

        List<BuildResult> oldest = buildResultDao.findOldestByProject(p1, ResultState.getCompletedStates(), 1, false);
        assertEquals(0, oldest.size());
    }

    public void testGetOldestBuildsExcludesPersonal()
    {
        User u1 = new User("u1", "u1");
        userDao.save(u1);

        Project p1 = new Project();
        projectDao.save(p1);

        BuildResult r1 = createCompletedBuild(p1, 1);
        BuildResult r2 = createCompletedBuild(p1, 2);
        BuildResult r3 = createPersonalBuild(u1, p1, 1);

        buildResultDao.save(r1);
        buildResultDao.save(r2);
        buildResultDao.save(r3);

        commitAndRefreshTransaction();

        List<BuildResult> oldest = buildResultDao.findOldestByProject(p1, null, 3, false);
        assertEquals(2, oldest.size());
        assertPropertyEquals(r1, oldest.get(0));
        assertPropertyEquals(r2, oldest.get(1));
    }

    public void testGetOldestBuildsIncludesPersonal()
    {
        User u1 = new User("u1", "u1");
        userDao.save(u1);

        Project p1 = new Project();
        projectDao.save(p1);

        BuildResult r1 = createCompletedBuild(p1, 1);
        BuildResult r2 = createCompletedBuild(p1, 2);
        BuildResult r3 = createPersonalBuild(u1, p1, 1);

        buildResultDao.save(r1);
        buildResultDao.save(r2);
        buildResultDao.save(r3);

        commitAndRefreshTransaction();

        List<BuildResult> oldest = buildResultDao.findOldestByProject(p1, null, 3, true);
        assertEquals(3, oldest.size());
        assertPropertyEquals(r1, oldest.get(0));
        assertPropertyEquals(r2, oldest.get(1));
        assertPropertyEquals(r3, oldest.get(2));
    }

    public void testGetPreviousBuildResult()
    {
        Project p1 = new Project();
        projectDao.save(p1);

        BuildResult resultA = new BuildResult(new TriggerBuildReason("scm trigger"), p1, makeSpec(), 1, false);
        buildResultDao.save(resultA);
        BuildResult resultB = new BuildResult(new TriggerBuildReason("scm trigger"), p1, makeSpec(), 2, false);
        buildResultDao.save(resultB);
        BuildResult resultC = new BuildResult(new TriggerBuildReason("scm trigger"), p1, makeSpec(), 3, false);
        buildResultDao.save(resultC);

        commitAndRefreshTransaction();

        assertNull(buildResultDao.findPreviousBuildResult(resultA));
        assertEquals(resultA, buildResultDao.findPreviousBuildResult(resultB));
        assertEquals(resultB, buildResultDao.findPreviousBuildResult(resultC));
    }

    public void testGetLatestCompletedSimple()
    {
        Project p1 = new Project();
        Project p2 = new Project();
        projectDao.save(p1);
        projectDao.save(p2);

        BuildSpecification b1 = new BuildSpecification("b1");
        BuildSpecification b2 = new BuildSpecification("b2");
        buildSpecificationDao.save(b1);
        buildSpecificationDao.save(b2);

        BuildResult r1 = createCompletedBuild(p1, b1, 1);
        BuildResult r2 = createCompletedBuild(p1, b1, 2);
        BuildResult r3 = createCompletedBuild(p1, b2, 3);
        BuildResult r4 = createCompletedBuild(p2, b1, 3);

        buildResultDao.save(r1);
        buildResultDao.save(r2);
        buildResultDao.save(r3);
        buildResultDao.save(r4);

        commitAndRefreshTransaction();

        List<BuildResult> latestCompleted = buildResultDao.findLatestCompleted(p1, b1.getPname(), 0, 10);
        assertEquals(2, latestCompleted.size());
        assertPropertyEquals(r2, latestCompleted.get(0));
        assertPropertyEquals(r1, latestCompleted.get(1));
    }

    public void testGetLatestCompletedInitial()
    {
        Project p1 = new Project();
        projectDao.save(p1);

        BuildSpecification b1 = new BuildSpecification("b1");
        buildSpecificationDao.save(b1);

        BuildResult r1 = createCompletedBuild(p1, b1, 1);
        BuildResult r2 = new BuildResult(new TriggerBuildReason("scm trigger"), p1, b1, 2, false);

        buildResultDao.save(r1);
        buildResultDao.save(r2);

        commitAndRefreshTransaction();

        List<BuildResult> latestCompleted = buildResultDao.findLatestCompleted(p1, b1.getPname(), 0, 10);
        assertEquals(1, latestCompleted.size());
        assertPropertyEquals(r1, latestCompleted.get(0));
    }

    public void testGetLatestCompletedInProgress()
    {
        Project p1 = new Project();
        projectDao.save(p1);

        BuildSpecification b1 = new BuildSpecification("b1");
        buildSpecificationDao.save(b1);

        BuildResult r1 = createCompletedBuild(p1, b1, 1);
        BuildResult r2 = new BuildResult(new TriggerBuildReason("scm trigger"), p1, b1, 2, false);
        r2.commence();

        buildResultDao.save(r1);
        buildResultDao.save(r2);

        commitAndRefreshTransaction();

        List<BuildResult> latestCompleted = buildResultDao.findLatestCompleted(p1, b1.getPname(), 0, 10);
        assertEquals(1, latestCompleted.size());
        assertPropertyEquals(r1, latestCompleted.get(0));
    }

    public void testGetLatestCompletedMax()
    {
        Project p1 = new Project();
        projectDao.save(p1);

        BuildSpecification b1 = new BuildSpecification("b1");
        buildSpecificationDao.save(b1);

        BuildResult r1 = createCompletedBuild(p1, b1, 1);
        BuildResult r2 = createCompletedBuild(p1, b1, 2);
        BuildResult r3 = createCompletedBuild(p1, b1, 3);
        BuildResult r4 = createCompletedBuild(p1, b1, 4);

        buildResultDao.save(r1);
        buildResultDao.save(r2);
        buildResultDao.save(r3);
        buildResultDao.save(r4);

        commitAndRefreshTransaction();

        List<BuildResult> latestCompleted = buildResultDao.findLatestCompleted(p1, b1.getPname(), 0, 2);
        assertEquals(2, latestCompleted.size());
        assertPropertyEquals(r4, latestCompleted.get(0));
        assertPropertyEquals(r3, latestCompleted.get(1));
    }

    public void testGetLatestCompletedFirst()
    {
        Project p1 = new Project();
        projectDao.save(p1);

        BuildSpecification b1 = new BuildSpecification("b1");
        buildSpecificationDao.save(b1);

        BuildResult r1 = createCompletedBuild(p1, b1, 1);
        BuildResult r2 = createCompletedBuild(p1, b1, 2);
        BuildResult r3 = createCompletedBuild(p1, b1, 3);
        BuildResult r4 = createCompletedBuild(p1, b1, 4);

        buildResultDao.save(r1);
        buildResultDao.save(r2);
        buildResultDao.save(r3);
        buildResultDao.save(r4);

        commitAndRefreshTransaction();

        List<BuildResult> latestCompleted = buildResultDao.findLatestCompleted(p1, b1.getPname(), 1, 4);
        assertEquals(3, latestCompleted.size());
        assertPropertyEquals(r3, latestCompleted.get(0));
        assertPropertyEquals(r2, latestCompleted.get(1));
        assertPropertyEquals(r1, latestCompleted.get(2));
    }

    public void testDeleteBuildRetainChangelist()
    {
        Project p = new Project("p", "test");
        projectDao.save(p);

        BuildResult result = createCompletedBuild(p, 1);
        result.setScmDetails(new BuildScmDetails(new NumericalRevision(10)));
        buildResultDao.save(result);

        Changelist list = new Changelist("uid", new NumericalRevision(10));
        list.addProjectId(p.getId());
        list.addResultId(result.getId());
        changelistDao.save(list);

        commitAndRefreshTransaction();
        assertNotNull(changelistDao.findById(list.getId()));
        commitAndRefreshTransaction();

        buildResultDao.delete(result);
        commitAndRefreshTransaction();
        assertNotNull(changelistDao.findById(list.getId()));
    }

    public void testFindByUser()
    {
        Project p = new Project("p", "test");
        projectDao.save(p);

        User u1 = new User("u1", "u1");
        User u2 = new User("u2", "u2");
        User u3 = new User("u3", "u3");
        userDao.save(u1);
        userDao.save(u2);
        userDao.save(u3);

        BuildResult r1 = createCompletedBuild(p, 1);
        BuildResult r2 = createPersonalBuild(u1, p, 1);
        BuildResult r3 = createPersonalBuild(u2, p, 1);
        BuildResult r4 = createPersonalBuild(u2, p, 2);
        buildResultDao.save(r1);
        buildResultDao.save(r2);
        buildResultDao.save(r3);
        buildResultDao.save(r4);

        commitAndRefreshTransaction();

        List<BuildResult> results = buildResultDao.findByUser(u1);
        assertEquals(1, results.size());
        assertEquals(u1, results.get(0).getUser());

        results = buildResultDao.findByUser(u2);
        assertEquals(2, results.size());
        assertEquals(u2, results.get(0).getUser());
        assertEquals(2, results.get(0).getNumber());
        assertEquals(u2, results.get(1).getUser());

        results = buildResultDao.findByUser(u3);
        assertEquals(0, results.size());
    }

    public void testFindByUserAndNumber()
    {
        Project p = new Project("p", "test");
        projectDao.save(p);

        User u1 = new User("u1", "u1");
        User u2 = new User("u2", "u2");
        User u3 = new User("u3", "u3");
        userDao.save(u1);
        userDao.save(u2);
        userDao.save(u3);

        BuildResult r1 = createCompletedBuild(p, 1);
        BuildResult r2 = createPersonalBuild(u1, p, 1);
        BuildResult r3 = createPersonalBuild(u2, p, 1);
        BuildResult r4 = createPersonalBuild(u2, p, 2);
        buildResultDao.save(r1);
        buildResultDao.save(r2);
        buildResultDao.save(r3);
        buildResultDao.save(r4);

        commitAndRefreshTransaction();

        BuildResult result = buildResultDao.findByUserAndNumber(u1, 1);
        assertNotNull(result);
        assertEquals(u1, result.getUser());
        assertEquals(1, result.getNumber());

        result = buildResultDao.findByUserAndNumber(u2, 2);
        assertNotNull(result);
        assertEquals(u2, result.getUser());
        assertEquals(2, result.getNumber());

        result = buildResultDao.findByUserAndNumber(u1, 2);
        assertNull(result);
    }

    public void testGetLatestByUser()
    {
        Project p = new Project("p", "test");
        projectDao.save(p);

        User u1 = new User("u1", "u1");
        User u2 = new User("u2", "u2");
        userDao.save(u1);
        userDao.save(u2);

        BuildResult r1 = createPersonalBuild(u1, p, 1);
        BuildResult r2 = createPersonalBuild(u2, p, 1);
        BuildResult r3 = createPersonalBuild(u2, p, 2);
        buildResultDao.save(r1);
        buildResultDao.save(r2);
        buildResultDao.save(r3);

        commitAndRefreshTransaction();

        List<BuildResult> results = buildResultDao.getLatestByUser(u1, null, 1);
        assertEquals(1, results.size());
        assertEquals(u1, results.get(0).getUser());

        results = buildResultDao.getLatestByUser(u2, null, 1);
        assertEquals(1, results.size());
        assertEquals(u2, results.get(0).getUser());
        assertEquals(2, results.get(0).getNumber());
    }

    public void testGetLatestByUserStates()
    {
        Project p = new Project("p", "test");
        projectDao.save(p);

        User u1 = new User("u1", "u1");
        userDao.save(u1);

        BuildResult r1 = createPersonalBuild(u1, p, 1);
        BuildResult r2 = createIncompletePersonalBuild(u1, p, 2);
        buildResultDao.save(r1);
        buildResultDao.save(r2);

        commitAndRefreshTransaction();

        List<BuildResult> results = buildResultDao.getLatestByUser(u1, ResultState.getCompletedStates(), 1);
        assertEquals(1, results.size());
        assertEquals(1, results.get(0).getNumber());
        assertEquals(u1, results.get(0).getUser());
    }

    public void testGetCompletedPersonalBuildCount()
    {
        Project p = new Project("p", "test");
        projectDao.save(p);

        User u1 = new User("u1", "u1");
        User u2 = new User("u2", "u2");
        userDao.save(u1);
        userDao.save(u2);

        BuildResult r1 = createPersonalBuild(u1, p, 1);
        BuildResult r2 = createIncompletePersonalBuild(u1, p, 2);
        BuildResult r3 = createPersonalBuild(u2, p, 1);
        BuildResult r4 = createPersonalBuild(u2, p, 2);
        buildResultDao.save(r1);
        buildResultDao.save(r2);
        buildResultDao.save(r3);
        buildResultDao.save(r4);

        commitAndRefreshTransaction();

        assertEquals(1, buildResultDao.getCompletedResultCount(u1));
        assertEquals(2, buildResultDao.getCompletedResultCount(u2));
    }

    public void testGetOldestCompletedPersonalBuilds()
    {
        Project p = new Project("p", "test");
        projectDao.save(p);

        User u1 = new User("u1", "u1");
        User u2 = new User("u2", "u2");
        userDao.save(u1);
        userDao.save(u2);

        BuildResult r1 = createPersonalBuild(u1, p, 1);
        BuildResult r2 = createIncompletePersonalBuild(u1, p, 2);
        BuildResult r3 = createPersonalBuild(u2, p, 1);
        BuildResult r4 = createPersonalBuild(u2, p, 2);
        buildResultDao.save(r1);
        buildResultDao.save(r2);
        buildResultDao.save(r3);
        buildResultDao.save(r4);

        commitAndRefreshTransaction();

        List<BuildResult> results = buildResultDao.getOldestCompletedBuilds(u1, -1);
        assertEquals(1, results.size());
        assertEquals(u1, results.get(0).getUser());
        assertEquals(1, results.get(0).getNumber());

        results = buildResultDao.getOldestCompletedBuilds(u2, -1);
        assertEquals(2, results.size());
        assertEquals(u2, results.get(0).getUser());
        assertEquals(1, results.get(0).getNumber());
        assertEquals(u2, results.get(1).getUser());
        assertEquals(2, results.get(1).getNumber());

        results = buildResultDao.getOldestCompletedBuilds(u2, 1);
        assertEquals(1, results.size());
        assertEquals(u2, results.get(0).getUser());
        assertEquals(1, results.get(0).getNumber());
    }

    private void createFindLatestSuccessfulTestData()
    {
        Project projectA = new Project("a", "");
        projectA.addBuildSpecification(new BuildSpecification("a1"));
        projectA.addBuildSpecification(new BuildSpecification("a2"));
        projectDao.save(projectA);

        Project projectB = new Project("b", "");
        projectB.addBuildSpecification(new BuildSpecification("b1"));
        projectB.addBuildSpecification(new BuildSpecification("b2"));
        projectDao.save(projectB);

        commitAndRefreshTransaction();

        // create successful and failed builds.
        buildResultDao.save(createFailedBuild(projectA, projectA.getBuildSpecification("a1"), 1)); // failed
        buildResultDao.save(createFailedBuild(projectA, projectA.getBuildSpecification("a2"), 2)); // failed
        buildResultDao.save(createCompletedBuild(projectA, projectA.getBuildSpecification("a1"), 3)); // success
        buildResultDao.save(createCompletedBuild(projectA, projectA.getBuildSpecification("a2"), 4)); // success
        buildResultDao.save(createFailedBuild(projectA, projectA.getBuildSpecification("a1"), 5)); // failed
        buildResultDao.save(createFailedBuild(projectA, projectA.getBuildSpecification("a2"), 6)); // failed

        buildResultDao.save(createFailedBuild(projectB, projectB.getBuildSpecification("b1"), 1));
        buildResultDao.save(createFailedBuild(projectB, projectB.getBuildSpecification("b2"), 2));
        buildResultDao.save(createCompletedBuild(projectB, projectB.getBuildSpecification("b1"), 3));
        buildResultDao.save(createCompletedBuild(projectB, projectB.getBuildSpecification("b2"), 4));
        buildResultDao.save(createFailedBuild(projectB, projectB.getBuildSpecification("b1"), 5));
        buildResultDao.save(createFailedBuild(projectB, projectB.getBuildSpecification("b2"), 6));

        commitAndRefreshTransaction();
    }

    public void testFindLatestSuccessful()
    {
        createFindLatestSuccessfulTestData();

        BuildResult result = buildResultDao.findLatestSuccessful();
        assertEquals(4, result.getNumber());
        assertEquals("b2", result.getSpecName().getName());
    }

    public void testFindLatestSuccessfulByProject()
    {
        createFindLatestSuccessfulTestData();

        Project projectA = projectDao.findByName("a");
        BuildResult result = buildResultDao.findLatestSuccessfulByProject(projectA);
        assertEquals(4, result.getNumber());
        assertEquals("a2", result.getSpecName().getName());

        Project projectB = projectDao.findByName("b");
        result = buildResultDao.findLatestSuccessfulByProject(projectB);
        assertEquals(4, result.getNumber());
        assertEquals("b2", result.getSpecName().getName());
    }

    public void testFindLatestSuccessfulBySpecification()
    {
        createFindLatestSuccessfulTestData();

        BuildSpecification specA1 = null;
        BuildSpecification specA2 = null;

        List<BuildSpecification> specs = buildSpecificationDao.findAll();
        for (BuildSpecification spec : specs)
        {
            if (spec.getName().endsWith("a1"))
            {
                specA1 = spec;
            }
            if (spec.getName().endsWith("a2"))
            {
                specA2 = spec;
            }
        }

        BuildResult result = buildResultDao.findLatestSuccessfulBySpecification(specA1);
        assertEquals(3, result.getNumber());
        assertEquals("a1", result.getSpecName().getName());

        result = buildResultDao.findLatestSuccessfulBySpecification(specA2);
        assertEquals(4, result.getNumber());
        assertEquals("a2", result.getSpecName().getName());
    }

    public void testGetBuildCountRange()
    {
        Project p1 = new Project("p1", "this is p1 speaking");
        BuildSpecification s1 = new BuildSpecification("s1");
        BuildSpecification s2 = new BuildSpecification("s2");
        p1.addBuildSpecification(s1);
        p1.addBuildSpecification(s2);
        projectDao.save(p1);

        buildResultDao.save(createCompletedBuild(p1, s1, 1));
        buildResultDao.save(createCompletedBuild(p1, s2, 2));
        buildResultDao.save(createCompletedBuild(p1, s1, 3));
        buildResultDao.save(createCompletedBuild(p1, s1, 4));
        buildResultDao.save(createCompletedBuild(p1, s2, 5));
        
        assertEquals(0, buildResultDao.getBuildCount(s1.getPname(), 1, 1));
        assertEquals(1, buildResultDao.getBuildCount(s1.getPname(), 0, 1));
        assertEquals(1, buildResultDao.getBuildCount(s1.getPname(), 0, 2));
        assertEquals(2, buildResultDao.getBuildCount(s1.getPname(), 0, 3));
        assertEquals(1, buildResultDao.getBuildCount(s1.getPname(), 1, 3));
        assertEquals(2, buildResultDao.getBuildCount(s1.getPname(), 1, 4));
        assertEquals(2, buildResultDao.getBuildCount(s1.getPname(), 1, 100));
        assertEquals(3, buildResultDao.getBuildCount(s1.getPname(), 0, 100));
    }

    public void testQuerySpecificationBuilds()
    {
        Project p1 = new Project("p1", "this is p1 speaking");
        BuildSpecification s1 = new BuildSpecification("s1");
        BuildSpecification s2 = new BuildSpecification("s2");
        p1.addBuildSpecification(s1);
        p1.addBuildSpecification(s2);
        projectDao.save(p1);

        buildResultDao.save(createCompletedBuild(p1, s1, 1));
        buildResultDao.save(createCompletedBuild(p1, s2, 2));
        buildResultDao.save(createCompletedBuild(p1, s1, 3));
        buildResultDao.save(createCompletedBuild(p1, s1, 4));
        buildResultDao.save(createCompletedBuild(p1, s2, 5));

        List<BuildResult> results = buildResultDao.querySpecificationBuilds(p1, s1.getPname(), null, 1, -1, 0, 1, false, false);
        assertEquals(1, results.size());
        assertEquals(1, results.get(0).getNumber());

        results = buildResultDao.querySpecificationBuilds(p1, s1.getPname(), null, 2, -1, 0, 1, false, false);
        assertEquals(1, results.size());
        assertEquals(3, results.get(0).getNumber());

        results = buildResultDao.querySpecificationBuilds(p1, s1.getPname(), null, 3, -1, 0, 1, false, false);
        assertEquals(1, results.size());
        assertEquals(3, results.get(0).getNumber());

        results = buildResultDao.querySpecificationBuilds(p1, s1.getPname(), null, 4, -1, 0, 1, false, false);
        assertEquals(1, results.size());
        assertEquals(4, results.get(0).getNumber());

        results = buildResultDao.querySpecificationBuilds(p1, s1.getPname(), null, 5, -1, 0, 1, false, false);
        assertEquals(0, results.size());
    }

    private BuildResult createCompletedBuild(Project project, long number)
    {
        BuildSpecification spec = makeSpec();
        return createCompletedBuild(project, spec, number);
    }

    private BuildSpecification makeSpec()
    {
        BuildSpecification spec = new BuildSpecification("test spec");
        buildSpecificationDao.save(spec);
        return spec;
    }

    private BuildResult createCompletedBuild(Project project, BuildSpecification spec, long number)
    {
        BuildResult result = new BuildResult(new TriggerBuildReason("scm trigger"), project, spec, number, false);
        result.commence(time++);
        result.complete(time++);
        return result;
    }

    private BuildResult createFailedBuild(Project project, BuildSpecification spec, long number)
    {
        BuildResult result = new BuildResult(new TriggerBuildReason("scm trigger"), project, spec, number, false);
        result.commence(time++);
        result.failure();
        result.complete(time++);
        return result;
    }

    private BuildResult createPersonalBuild(User user, Project project, long number)
    {
        BuildResult result = createIncompletePersonalBuild(user, project, number);
        result.complete(time++);
        return result;
    }

    private BuildResult createIncompletePersonalBuild(User user, Project project, long number)
    {
        BuildResult result = new BuildResult(user, project, makeSpec(), number);
        result.commence(time++);
        return result;
    }
}
