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
        BuildSpecification spec = new BuildSpecification();
        buildSpecificationDao.save(spec);

        BuildResult buildResult = new BuildResult(new TriggerBuildReason("scm trigger"), project, spec.getName(), 11);
        buildResult.commence();
        buildResult.setScmDetails(scmDetails);
        RecipeResultNode recipeNode = new RecipeResultNode("test", recipeResult);
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

        List<BuildResult> oldest = buildResultDao.findOldestByProject(p1, 1);
        assertEquals(1, oldest.size());
        assertPropertyEquals(r1, oldest.get(0));

        oldest = buildResultDao.findOldestByProject(p1, 3);
        assertEquals(3, oldest.size());
        assertPropertyEquals(r1, oldest.get(0));
        assertPropertyEquals(r2, oldest.get(1));
        assertPropertyEquals(r3, oldest.get(2));

        oldest = buildResultDao.findOldestByProject(p1, 100);
        assertEquals(4, oldest.size());
    }

    public void testGetOldestBuildsPaged()
    {
        Project p1 = new Project();
        projectDao.save(p1);

        BuildResult r1 = createCompletedBuild(p1, 1);
        BuildResult r2 = createCompletedBuild(p1, 2);
        BuildResult r3 = createCompletedBuild(p1, 3);
        BuildResult r4 = createCompletedBuild(p1, 4);

        buildResultDao.save(r1);
        buildResultDao.save(r2);
        buildResultDao.save(r3);
        buildResultDao.save(r4);

        commitAndRefreshTransaction();

        List<BuildResult> oldest = buildResultDao.findOldestByProject(p1, 1, 2);
        assertEquals(2, oldest.size());
        assertPropertyEquals(r2, oldest.get(0));
        assertPropertyEquals(r3, oldest.get(1));
    }

    public void testGetOldestBuildsInitial()
    {
        Project p1 = new Project();
        projectDao.save(p1);

        BuildResult result = new BuildResult(new TriggerBuildReason("scm trigger"), p1, null, 1);
        buildResultDao.save(result);

        commitAndRefreshTransaction();

        List<BuildResult> oldest = buildResultDao.findOldestByProject(p1, 1);
        assertEquals(0, oldest.size());
    }

    public void testGetOldestBuildsInProgress()
    {
        Project p1 = new Project();
        projectDao.save(p1);

        BuildResult result = new BuildResult(new TriggerBuildReason("scm trigger"), p1, null, 1);
        result.commence(0);
        buildResultDao.save(result);

        commitAndRefreshTransaction();

        List<BuildResult> oldest = buildResultDao.findOldestByProject(p1, 1);
        assertEquals(0, oldest.size());
    }

    public void testGetPreviousBuildResult()
    {
        Project p1 = new Project();
        projectDao.save(p1);

        BuildResult resultA = new BuildResult(new TriggerBuildReason("scm trigger"), p1, null, 1);
        buildResultDao.save(resultA);
        BuildResult resultB = new BuildResult(new TriggerBuildReason("scm trigger"), p1, null, 2);
        buildResultDao.save(resultB);
        BuildResult resultC = new BuildResult(new TriggerBuildReason("scm trigger"), p1, null, 3);
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

        List<BuildResult> latestCompleted = buildResultDao.findLatestCompleted(p1, b1.getName(), 0, 10);
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
        BuildResult r2 = new BuildResult(new TriggerBuildReason("scm trigger"), p1, b1.getName(), 2);

        buildResultDao.save(r1);
        buildResultDao.save(r2);

        commitAndRefreshTransaction();

        List<BuildResult> latestCompleted = buildResultDao.findLatestCompleted(p1, b1.getName(), 0, 10);
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
        BuildResult r2 = new BuildResult(new TriggerBuildReason("scm trigger"), p1, b1.getName(), 2);
        r2.commence();

        buildResultDao.save(r1);
        buildResultDao.save(r2);

        commitAndRefreshTransaction();

        List<BuildResult> latestCompleted = buildResultDao.findLatestCompleted(p1, b1.getName(), 0, 10);
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

        List<BuildResult> latestCompleted = buildResultDao.findLatestCompleted(p1, b1.getName(), 0, 2);
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

        List<BuildResult> latestCompleted = buildResultDao.findLatestCompleted(p1, b1.getName(), 1, 4);
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

        List<BuildResult> results = buildResultDao.getLatestByUser(u1, 1);
        assertEquals(1, results.size());
        assertEquals(u1, results.get(0).getUser());

        results = buildResultDao.getLatestByUser(u2, 1);
        assertEquals(1, results.size());
        assertEquals(u2, results.get(0).getUser());
        assertEquals(2, results.get(0).getNumber());
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

    private BuildResult createCompletedBuild(Project project, long number)
    {
        return createCompletedBuild(project, new BuildSpecification("test spec"), number);
    }

    private BuildResult createCompletedBuild(Project project, BuildSpecification spec, long number)
    {
        BuildResult result = new BuildResult(new TriggerBuildReason("scm trigger"), project, spec.getName(), number);
        result.commence(0);
        result.complete();
        return result;
    }

    private BuildResult createPersonalBuild(User user, Project project, long number)
    {
        BuildResult result = createIncompletePersonalBuild(user, project, number);
        result.complete();
        return result;
    }

    private BuildResult createIncompletePersonalBuild(User user, Project project, long number)
    {
        BuildResult result = new BuildResult(user, project, "spec", number);
        result.commence(0);
        return result;
    }
}
