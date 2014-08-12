package com.zutubi.pulse.master.model.persistence.hibernate;

import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.model.persistence.BuildResultDao;
import com.zutubi.pulse.master.model.persistence.ChangelistDao;
import com.zutubi.pulse.master.model.persistence.ProjectDao;
import com.zutubi.pulse.master.model.persistence.UserDao;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;


/**
 * See also the BuildQueryTest.
 */
public class HibernateBuildResultDaoTest extends MasterPersistenceTestCase
{
    private static final TriggerBuildReason TEST_REASON = new TriggerBuildReason("scm trigger");

    private static long time = 0;

    private BuildResultDao buildResultDao;
    private ChangelistDao changelistDao;
    private UserDao userDao;

    private Project projectA;
    private Project projectB;

    public void setUp() throws Exception
    {
        super.setUp();
        buildResultDao = (BuildResultDao) context.getBean("buildResultDao");
        ProjectDao projectDao = (ProjectDao) context.getBean("projectDao");
        changelistDao = (ChangelistDao) context.getBean("changelistDao");
        userDao = (UserDao) context.getBean("userDao");

        projectA = new Project();
        projectDao.save(projectA);

        projectB = new Project();
        projectDao.save(projectB);
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

        Revision revision = new Revision("42");

        BuildResult buildResult = new BuildResult(TEST_REASON, projectA, 11, false);
        buildResult.commence();
        buildResult.setRevision(revision);
        RecipeResultNode recipeNode = new RecipeResultNode("stage name", 123, recipeResult);
        recipeNode.setAgentName("test host");
        buildResult.addStage(recipeNode);

        buildResultDao.save(buildResult);
        commitAndRefreshTransaction();

        BuildResult anotherBuildResult = buildResultDao.findById(buildResult.getId());
        assertPropertyEquals(buildResult, anotherBuildResult);
    }

    public void testSaveAndLoadTestSummary()
    {
        TestResultSummary summary = new TestResultSummary(88, 3, 323, 8, 111111);
        RecipeResult result = createRecipe();
        result.setTestSummary(summary);
        saveAndLoadRecipe(result);
    }

    public void testFindResultNodeByResultId()
    {
        RecipeResult result = createRecipe();
        RecipeResultNode node = new RecipeResultNode("name", 1, result);
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
        result.complete();
        return result;
    }

    private CommandResult createFailedCommand()
    {
        CommandResult result;
        result = new CommandResult("command name");
        result.commence();
        result.failure("oh no!");
        result.complete();
        return result;
    }

    private CommandResult createArtifactCommand()
    {
        CommandResult result = new CommandResult("command name");
        result.commence();
        result.complete();
        StoredFileArtifact artifact = new StoredFileArtifact("to file");
        PersistentPlainFeature feature = new PersistentPlainFeature(Feature.Level.ERROR, "getSummary here", 7);

        artifact.addFeature(feature);
        result.addArtifact(new StoredArtifact("test", artifact));
        return result;
    }

    public void testGetOldestBuilds()
    {
        BuildResult r1 = createCompletedBuild(projectA, 1);
        BuildResult r2 = createCompletedBuild(projectA, 2);
        BuildResult r3 = createCompletedBuild(projectA, 3);
        BuildResult r4 = createCompletedBuild(projectA, 4);
        BuildResult otherP = createCompletedBuild(projectB, 1);

        buildResultDao.save(r1);
        buildResultDao.save(r2);
        buildResultDao.save(r3);
        buildResultDao.save(r4);
        buildResultDao.save(otherP);

        commitAndRefreshTransaction();

        List<BuildResult> oldest = buildResultDao.findOldestByProject(projectA, null, 1, false);
        assertEquals(1, oldest.size());
        assertPropertyEquals(r1, oldest.get(0));

        oldest = buildResultDao.findOldestByProject(projectA, null, 3, false);
        assertEquals(3, oldest.size());
        assertPropertyEquals(r1, oldest.get(0));
        assertPropertyEquals(r2, oldest.get(1));
        assertPropertyEquals(r3, oldest.get(2));

        oldest = buildResultDao.findOldestByProject(projectA, null, 100, false);
        assertEquals(4, oldest.size());
    }

    public void testGetOldestBuildsInitial()
    {
        BuildResult result = new BuildResult(TEST_REASON, projectA, 1, false);
        buildResultDao.save(result);

        commitAndRefreshTransaction();

        List<BuildResult> oldest = buildResultDao.findOldestByProject(projectA, ResultState.getCompletedStates(), 1, false);
        assertEquals(0, oldest.size());
    }

    public void testGetOldestBuildsInProgress()
    {
        BuildResult result = new BuildResult(TEST_REASON, projectA, 1, false);
        result.commence(0);
        buildResultDao.save(result);

        commitAndRefreshTransaction();

        List<BuildResult> oldest = buildResultDao.findOldestByProject(projectA, ResultState.getCompletedStates(), 1, false);
        assertEquals(0, oldest.size());
    }

    public void testGetOldestBuildsExcludesPersonal()
    {
        User u1 = new User();
        userDao.save(u1);

        BuildResult r1 = createCompletedBuild(projectA, 1);
        BuildResult r2 = createCompletedBuild(projectA, 2);
        BuildResult r3 = createPersonalBuild(u1, projectA, 1);

        buildResultDao.save(r1);
        buildResultDao.save(r2);
        buildResultDao.save(r3);

        commitAndRefreshTransaction();

        List<BuildResult> oldest = buildResultDao.findOldestByProject(projectA, null, 3, false);
        assertEquals(2, oldest.size());
        assertPropertyEquals(r1, oldest.get(0));
        assertPropertyEquals(r2, oldest.get(1));
    }

    public void testGetOldestBuildsIncludesPersonal()
    {
        User u1 = new User();
        userDao.save(u1);

        BuildResult r1 = createCompletedBuild(projectA, 1);
        BuildResult r2 = createCompletedBuild(projectA, 2);
        BuildResult r3 = createPersonalBuild(u1, projectA, 1);

        buildResultDao.save(r1);
        buildResultDao.save(r2);
        buildResultDao.save(r3);

        commitAndRefreshTransaction();

        List<BuildResult> oldest = buildResultDao.findOldestByProject(projectA, null, 3, true);
        assertEquals(3, oldest.size());
        assertPropertyEquals(r1, oldest.get(0));
        assertPropertyEquals(r2, oldest.get(1));
        assertPropertyEquals(r3, oldest.get(2));
    }

    public void testGetPreviousBuildResult()
    {
        BuildResult resultA = new BuildResult(TEST_REASON, projectA, 1, false);
        buildResultDao.save(resultA);
        BuildResult resultB = new BuildResult(TEST_REASON, projectA, 2, false);
        buildResultDao.save(resultB);
        BuildResult resultC = new BuildResult(TEST_REASON, projectA, 3, false);
        buildResultDao.save(resultC);

        commitAndRefreshTransaction();

        assertNull(buildResultDao.findPreviousBuildResult(resultA));
        assertEquals(resultA, buildResultDao.findPreviousBuildResult(resultB));
        assertEquals(resultB, buildResultDao.findPreviousBuildResult(resultC));
    }

    public void testGetPreviousBuildResultWithRevision()
    {
        BuildResult r1 = createCompletedBuild(projectA, 1);
        BuildResult r2 = createCompletedBuild(projectA, 2);
        BuildResult r3 = createCompletedBuild(projectA, 3);
        BuildResult other = createCompletedBuild(projectB, 1);

        buildResultDao.save(r1);
        buildResultDao.save(r2);
        buildResultDao.save(r3);
        buildResultDao.save(other);

        commitAndRefreshTransaction();

        assertNull(buildResultDao.findPreviousBuildResultWithRevision(r1, null));
        assertEquals(r1, buildResultDao.findPreviousBuildResultWithRevision(r2, null));
        assertEquals(r2, buildResultDao.findPreviousBuildResultWithRevision(r3, null));
    }

    public void testGetPreviousBuildResultWithRevisionFilterStates()
    {
        BuildResult r1 = createCompletedBuild(projectA, 1);
        BuildResult r2 = createFailedBuild(projectA, 2);
        BuildResult r3 = createCompletedBuild(projectA, 3);

        buildResultDao.save(r1);
        buildResultDao.save(r2);
        buildResultDao.save(r3);

        commitAndRefreshTransaction();

        assertNull(buildResultDao.findPreviousBuildResultWithRevision(r1, new ResultState[]{ResultState.SUCCESS}));
        assertEquals(r1, buildResultDao.findPreviousBuildResultWithRevision(r2, new ResultState[]{ResultState.SUCCESS}));
        assertEquals(r1, buildResultDao.findPreviousBuildResultWithRevision(r3, new ResultState[]{ResultState.SUCCESS}));
    }

    public void testGetPreviousBuildResultWithRevisionSkipsUserRevision()
    {
        BuildResult r1 = createUserRevisionBuild(projectA, 1);
        BuildResult r2 = createCompletedBuild(projectA, 2);
        BuildResult r3 = createUserRevisionBuild(projectA, 3);
        BuildResult r4 = createCompletedBuild(projectA, 4);

        buildResultDao.save(r1);
        buildResultDao.save(r2);
        buildResultDao.save(r3);
        buildResultDao.save(r4);

        commitAndRefreshTransaction();

        assertNull(buildResultDao.findPreviousBuildResultWithRevision(r2, null));
        assertEquals(r2, buildResultDao.findPreviousBuildResultWithRevision(r3, null));
        assertEquals(r2, buildResultDao.findPreviousBuildResultWithRevision(r4, null));
    }

    private BuildResult createUserRevisionBuild(Project project, long number)
    {
        BuildResult result = new BuildResult(TEST_REASON, project, number, true);
        result.commence(time++);
        result.complete(time++);
        result.setRevision(new Revision(number));
        return result;
    }

    public void testGetPreviousBuildResultWithRevisionSkipsNullRevision()
    {
        BuildResult r1 = createNullRevisionBuild(projectA, 1);
        BuildResult r2 = createCompletedBuild(projectA, 2);
        BuildResult r3 = createNullRevisionBuild(projectA, 3);
        BuildResult r4 = createCompletedBuild(projectA, 4);

        buildResultDao.save(r1);
        buildResultDao.save(r2);
        buildResultDao.save(r3);
        buildResultDao.save(r4);

        commitAndRefreshTransaction();

        assertNull(buildResultDao.findPreviousBuildResultWithRevision(r2, null));
        assertEquals(r2, buildResultDao.findPreviousBuildResultWithRevision(r3, null));
        assertEquals(r2, buildResultDao.findPreviousBuildResultWithRevision(r4, null));
    }

    private BuildResult createNullRevisionBuild(Project project, long number)
    {
        BuildResult result = new BuildResult(TEST_REASON, project, number, true);
        result.commence(time++);
        result.complete(time++);
        return result;
    }

    public void testGetPreviousBuildResultWithRevisionSkipsPersonal()
    {
        User u1 = new User();
        userDao.save(u1);
        BuildResult r1 = createPersonalBuild(u1, projectA, 1);
        BuildResult r2 = createCompletedBuild(projectA, 2);
        BuildResult r3 = createPersonalBuild(u1, projectA, 3);
        BuildResult r4 = createCompletedBuild(projectA, 4);

        buildResultDao.save(r1);
        buildResultDao.save(r2);
        buildResultDao.save(r3);
        buildResultDao.save(r4);

        commitAndRefreshTransaction();

        assertNull(buildResultDao.findPreviousBuildResultWithRevision(r2, null));
        assertEquals(r2, buildResultDao.findPreviousBuildResultWithRevision(r3, null));
        assertEquals(r2, buildResultDao.findPreviousBuildResultWithRevision(r4, null));
    }

    public void testGetLatestCompletedSimple()
    {
        BuildResult r1 = createCompletedBuild(projectA, 1);
        BuildResult r2 = createCompletedBuild(projectA, 2);
        BuildResult r3 = createCompletedBuild(projectA, 3);
        BuildResult r4 = createCompletedBuild(projectB, 3);

        buildResultDao.save(r1);
        buildResultDao.save(r2);
        buildResultDao.save(r3);
        buildResultDao.save(r4);

        commitAndRefreshTransaction();

        List<BuildResult> latestCompleted = buildResultDao.findLatestCompleted(projectA, 0, 10);
        assertEquals(3, latestCompleted.size());
        assertPropertyEquals(r3, latestCompleted.get(0));
        assertPropertyEquals(r2, latestCompleted.get(1));
    }

    public void testGetLatestCompletedInitial()
    {
        BuildResult r1 = createCompletedBuild(projectA, 1);
        BuildResult r2 = new BuildResult(TEST_REASON, projectA, 2, false);

        buildResultDao.save(r1);
        buildResultDao.save(r2);

        commitAndRefreshTransaction();

        List<BuildResult> latestCompleted = buildResultDao.findLatestCompleted(projectA, 0, 10);
        assertEquals(1, latestCompleted.size());
        assertPropertyEquals(r1, latestCompleted.get(0));
    }

    public void testGetLatestCompletedInProgress()
    {
        BuildResult r1 = createCompletedBuild(projectA, 1);
        BuildResult r2 = new BuildResult(TEST_REASON, projectA, 2, false);
        r2.commence();

        buildResultDao.save(r1);
        buildResultDao.save(r2);

        commitAndRefreshTransaction();

        List<BuildResult> latestCompleted = buildResultDao.findLatestCompleted(projectA, 0, 10);
        assertEquals(1, latestCompleted.size());
        assertPropertyEquals(r1, latestCompleted.get(0));
    }

    public void testGetLatestCompletedMax()
    {
        BuildResult r1 = createCompletedBuild(projectA, 1);
        BuildResult r2 = createCompletedBuild(projectA, 2);
        BuildResult r3 = createCompletedBuild(projectA, 3);
        BuildResult r4 = createCompletedBuild(projectA, 4);

        buildResultDao.save(r1);
        buildResultDao.save(r2);
        buildResultDao.save(r3);
        buildResultDao.save(r4);

        commitAndRefreshTransaction();

        List<BuildResult> latestCompleted = buildResultDao.findLatestCompleted(projectA, 0, 2);
        assertEquals(2, latestCompleted.size());
        assertPropertyEquals(r4, latestCompleted.get(0));
        assertPropertyEquals(r3, latestCompleted.get(1));
    }

    public void testGetLatestCompletedFirst()
    {
        BuildResult r1 = createCompletedBuild(projectA, 1);
        BuildResult r2 = createCompletedBuild(projectA, 2);
        BuildResult r3 = createCompletedBuild(projectA, 3);
        BuildResult r4 = createCompletedBuild(projectA, 4);

        buildResultDao.save(r1);
        buildResultDao.save(r2);
        buildResultDao.save(r3);
        buildResultDao.save(r4);

        commitAndRefreshTransaction();

        List<BuildResult> latestCompleted = buildResultDao.findLatestCompleted(projectA, 1, 4);
        assertEquals(3, latestCompleted.size());
        assertPropertyEquals(r3, latestCompleted.get(0));
        assertPropertyEquals(r2, latestCompleted.get(1));
        assertPropertyEquals(r1, latestCompleted.get(2));
    }

    public void testDeleteBuildRetainChangelist()
    {
        BuildResult result = createCompletedBuild(projectA, 1);
        result.setRevision(new Revision("10"));
        buildResultDao.save(result);

        PersistentChangelist list = new PersistentChangelist(new Revision("10"), 0, null, null, Collections.<PersistentFileChange>emptyList());
        list.setProjectId(projectA.getId());
        list.setResultId(result.getId());
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
        User u1 = new User();
        User u2 = new User();
        User u3 = new User();
        userDao.save(u1);
        userDao.save(u2);
        userDao.save(u3);

        BuildResult r1 = createCompletedBuild(projectA, 1);
        BuildResult r2 = createPersonalBuild(u1, projectA, 1);
        BuildResult r3 = createPersonalBuild(u2, projectA, 1);
        BuildResult r4 = createPersonalBuild(u2, projectA, 2);
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
        User u1 = new User();
        User u2 = new User();
        User u3 = new User();
        userDao.save(u1);
        userDao.save(u2);
        userDao.save(u3);

        BuildResult r1 = createCompletedBuild(projectA, 1);
        BuildResult r2 = createPersonalBuild(u1, projectA, 1);
        BuildResult r3 = createPersonalBuild(u2, projectA, 1);
        BuildResult r4 = createPersonalBuild(u2, projectA, 2);
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
        User u1 = new User();
        User u2 = new User();
        userDao.save(u1);
        userDao.save(u2);

        BuildResult r1 = createPersonalBuild(u1, projectA, 1);
        BuildResult r2 = createPersonalBuild(u2, projectA, 1);
        BuildResult r3 = createPersonalBuild(u2, projectA, 2);
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
        User u1 = new User();
        userDao.save(u1);

        BuildResult r1 = createPersonalBuild(u1, projectA, 1);
        BuildResult r2 = createIncompletePersonalBuild(u1, projectA, 2);
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
        User u1 = new User();
        User u2 = new User();
        userDao.save(u1);
        userDao.save(u2);

        BuildResult r1 = createPersonalBuild(u1, projectA, 1);
        BuildResult r2 = createIncompletePersonalBuild(u1, projectA, 2);
        BuildResult r3 = createPersonalBuild(u2, projectA, 1);
        BuildResult r4 = createPersonalBuild(u2, projectA, 2);
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
        User u1 = new User();
        User u2 = new User();
        userDao.save(u1);
        userDao.save(u2);

        BuildResult r1 = createPersonalBuild(u1, projectA, 1);
        BuildResult r2 = createIncompletePersonalBuild(u1, projectA, 2);
        BuildResult r3 = createPersonalBuild(u2, projectA, 1);
        BuildResult r4 = createPersonalBuild(u2, projectA, 2);
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
        commitAndRefreshTransaction();

        // create successful and failed builds.
        buildResultDao.save(createFailedBuild(projectA, 1)); // failed
        buildResultDao.save(createFailedBuild(projectA, 2)); // failed
        buildResultDao.save(createCompletedBuild(projectA, 3)); // success
        buildResultDao.save(createCompletedBuild(projectA, 4)); // success
        buildResultDao.save(createFailedBuild(projectA, 5)); // failed
        buildResultDao.save(createFailedBuild(projectA, 6)); // failed

        buildResultDao.save(createFailedBuild(projectB, 1));
        buildResultDao.save(createFailedBuild(projectB, 2));
        buildResultDao.save(createCompletedBuild(projectB, 3));
        buildResultDao.save(createCompletedBuild(projectB, 4));
        buildResultDao.save(createFailedBuild(projectB, 5));
        buildResultDao.save(createFailedBuild(projectB, 6));

        commitAndRefreshTransaction();
    }

    public void testFindLatestSuccessfulByProject()
    {
        createFindLatestSuccessfulTestData();

        BuildResult result = buildResultDao.findLatestByProject(projectA, false, ResultState.SUCCESS);
        assertEquals(4, result.getNumber());

        result = buildResultDao.findLatestByProject(projectB, false, ResultState.SUCCESS);
        assertEquals(4, result.getNumber());
    }

    public void testGetBuildCountRange()
    {
        buildResultDao.save(createCompletedBuild(projectA, 1));
        buildResultDao.save(createCompletedBuild(projectA, 2));
        buildResultDao.save(createCompletedBuild(projectA, 3));
        buildResultDao.save(createCompletedBuild(projectA, 4));
        buildResultDao.save(createCompletedBuild(projectA, 5));

        buildResultDao.save(createCompletedBuild(projectB, 1));
        buildResultDao.save(createCompletedBuild(projectB, 2));
        buildResultDao.save(createCompletedBuild(projectB, 3));

        commitAndRefreshTransaction();

        assertEquals(0, buildResultDao.getBuildCount(projectA, 1, 1));
        assertEquals(1, buildResultDao.getBuildCount(projectA, 0, 1));
        assertEquals(2, buildResultDao.getBuildCount(projectA, 0, 2));
        assertEquals(3, buildResultDao.getBuildCount(projectA, 0, 3));
        assertEquals(2, buildResultDao.getBuildCount(projectA, 1, 3));
        assertEquals(3, buildResultDao.getBuildCount(projectA, 1, 4));
        assertEquals(4, buildResultDao.getBuildCount(projectA, 1, 100));
        assertEquals(5, buildResultDao.getBuildCount(projectA, 0, 100));
    }

    public void testGetBuildCountPinned()
    {
        buildResultDao.save(createCompletedBuild(projectA, 1));
        BuildResult build = createCompletedBuild(projectA, 2);
        build.setPinned(true);
        buildResultDao.save(build);
        buildResultDao.save(createCompletedBuild(projectA, 3));
        build = createCompletedBuild(projectA, 4);
        build.setPinned(true);
        buildResultDao.save(build);
        buildResultDao.save(createCompletedBuild(projectA, 5));

        assertEquals(5, buildResultDao.getBuildCount(projectA, null, null, true));
        assertEquals(3, buildResultDao.getBuildCount(projectA, null, null, false));
    }

    public void testQueryBuilds()
    {
        buildResultDao.save(createCompletedBuild(projectA, 1));
        buildResultDao.save(createCompletedBuild(projectA, 2));
        buildResultDao.save(createCompletedBuild(projectA, 3));
        buildResultDao.save(createCompletedBuild(projectA, 4));

        commitAndRefreshTransaction();

        List<BuildResult> results = buildResultDao.queryBuilds(projectA, null, 1, -1, 0, 1, false, false);
        assertEquals(1, results.size());
        assertEquals(1, results.get(0).getNumber());

        results = buildResultDao.queryBuilds(projectA, null, 2, -1, 0, 1, false, false);
        assertEquals(1, results.size());
        assertEquals(2, results.get(0).getNumber());

        results = buildResultDao.queryBuilds(projectA, null, 3, -1, 0, 1, false, false);
        assertEquals(1, results.size());
        assertEquals(3, results.get(0).getNumber());

        results = buildResultDao.queryBuilds(projectA, null, 4, -1, 0, 1, false, false);
        assertEquals(1, results.size());
        assertEquals(4, results.get(0).getNumber());
    }

    public void testQueryBuildsSuccess()
    {
        buildResultDao.save(createCompletedBuild(projectA, 1));
        buildResultDao.save(createCompletedBuild(projectA, 2));
        buildResultDao.save(createFailedBuild(projectA, 3));
        buildResultDao.save(createCompletedBuild(projectA, 4));
        buildResultDao.save(createCompletedBuild(projectA, 5));
        buildResultDao.save(createFailedBuild(projectA, 6));
        buildResultDao.save(createCompletedBuild(projectA, 7));

        commitAndRefreshTransaction();
        
        List<BuildResult> results = buildResultDao.queryBuilds(projectA, new ResultState[]{ ResultState.SUCCESS }, -1, 1, 0, 1, true, false);
        assertEquals(1, results.size());
        assertEquals(1, results.get(0).getNumber());

        results = buildResultDao.queryBuilds(projectA, new ResultState[]{ ResultState.SUCCESS }, -1, 2, 0, 1, true, false);
        assertEquals(1, results.size());
        assertEquals(2, results.get(0).getNumber());

        results = buildResultDao.queryBuilds(projectA, new ResultState[]{ ResultState.SUCCESS }, -1, 3, 0, 1, true, false);
        assertEquals(1, results.size());
        assertEquals(2, results.get(0).getNumber());

        results = buildResultDao.queryBuilds(projectA, new ResultState[]{ ResultState.SUCCESS }, -1, 4, 0, 1, true, false);
        assertEquals(1, results.size());
        assertEquals(4, results.get(0).getNumber());

        results = buildResultDao.queryBuilds(projectA, new ResultState[]{ ResultState.SUCCESS }, -1, 5, 0, 1, true, false);
        assertEquals(1, results.size());
        assertEquals(5, results.get(0).getNumber());

        results = buildResultDao.queryBuilds(projectA, new ResultState[]{ ResultState.SUCCESS }, -1, 6, 0, 1, true, false);
        assertEquals(1, results.size());
        assertEquals(5, results.get(0).getNumber());

        results = buildResultDao.queryBuilds(projectA, new ResultState[]{ ResultState.SUCCESS }, -1, 7, 0, 1, true, false);
        assertEquals(1, results.size());
        assertEquals(7, results.get(0).getNumber());
    }

    public void testQueryBuildsWithMessagesWarnings()
    {
        addMessageBuild(projectA, Feature.Level.WARNING, 1);

        List<BuildResult> results = buildResultDao.queryBuildsWithMessages(new Project[]{projectA}, Feature.Level.WARNING, 1);
        assertEquals(1, results.size());
        assertEquals(1, results.get(0).getNumber());

        results = buildResultDao.queryBuildsWithMessages(new Project[]{projectA}, Feature.Level.ERROR, 1);
        assertEquals(0, results.size());
    }

    public void testQueryBuildsWithMessagesErrors()
    {
        addMessageBuild(projectA, Feature.Level.ERROR, 1);

        List<BuildResult> results = buildResultDao.queryBuildsWithMessages(new Project[]{projectA}, Feature.Level.ERROR, 1);
        assertEquals(1, results.size());
        assertEquals(1, results.get(0).getNumber());

        results = buildResultDao.queryBuildsWithMessages(new Project[]{projectA}, Feature.Level.WARNING, 1);
        assertEquals(0, results.size());
    }

    public void testQueryBuildsWithMessagesMultiple()
    {
        addMessageBuild(projectA, Feature.Level.ERROR, 1);
        addMessageBuild(projectA, Feature.Level.ERROR, 2);
        addMessageBuild(projectB, Feature.Level.WARNING, 1);
        addMessageBuild(projectB, Feature.Level.ERROR, 2);
        addMessageBuild(projectB, Feature.Level.WARNING, 3);

        List<BuildResult> results = buildResultDao.queryBuildsWithMessages(new Project[]{projectA, projectB}, Feature.Level.ERROR, 10);
        assertEquals(3, results.size());
        assertEquals(2, results.get(0).getNumber());
        assertEquals(projectB, results.get(0).getProject());
        assertEquals(2, results.get(1).getNumber());
        assertEquals(projectA, results.get(1).getProject());
        assertEquals(1, results.get(2).getNumber());
        assertEquals(projectA, results.get(2).getProject());

        results = buildResultDao.queryBuildsWithMessages(new Project[]{projectA, projectB}, Feature.Level.ERROR, 1);
        assertEquals(1, results.size());
        assertEquals(2, results.get(0).getNumber());
        assertEquals(projectB, results.get(0).getProject());

        results = buildResultDao.queryBuildsWithMessages(new Project[]{projectB}, Feature.Level.WARNING, 10);
        assertEquals(2, results.size());
        assertEquals(3, results.get(0).getNumber());
        assertEquals(projectB, results.get(0).getProject());
        assertEquals(1, results.get(1).getNumber());
        assertEquals(projectB, results.get(1).getProject());
    }

    public void testQueryBuildsPinned()
    {
        buildResultDao.save(createCompletedBuild(projectA, 1));
        BuildResult build = createCompletedBuild(projectA, 2);
        build.setPinned(true);
        buildResultDao.save(build);
        buildResultDao.save(createCompletedBuild(projectA, 3));
        build = createCompletedBuild(projectA, 4);
        build.setPinned(true);
        buildResultDao.save(build);
        buildResultDao.save(createCompletedBuild(projectA, 5));

        assertEquals(5, buildResultDao.queryBuilds(new Project[]{projectA}, null, null, -1, -1, -1, -1, true, true).size());
        assertEquals(3, buildResultDao.queryBuilds(new Project[]{projectA}, null, null, -1, -1, -1, -1, true, false).size());
    }

    public void testFindByRecipeId()
    {
        BuildResult result1 = createResultWithRecipes();
        BuildResult result2 = createResultWithRecipes();
        buildResultDao.save(result1);
        buildResultDao.save(result2);

        BuildResult found = buildResultDao.findByRecipeId(getRecipeId(result1, 0));
        assertEquals(result1, found);

        found = buildResultDao.findByRecipeId(getRecipeId(result1, 1));
        assertEquals(result1, found);

        found = buildResultDao.findByRecipeId(getRecipeId(result2, 0));
        assertEquals(result2, found);

        assertNull(buildResultDao.findByRecipeId(11223344L));
    }

    public void testCountByAgent()
    {
        BuildResult result1 = createCompletedBuild(projectA, 1);
        result1.addStage(createResultNode("stage1", 1, "test1", "agent1"));
        result1.addStage(createResultNode("stage2", 2, "test2", "agent2"));

        BuildResult result2 = createCompletedBuild(projectB, 1);
        result2.setState(ResultState.FAILURE);
        result2.addStage(createResultNode("stage2", 2, "test2", "agent2"));
        result2.addStage(createResultNode("stage3", 3, "test3", "agent3"));

        BuildResult result3 = createCompletedBuild(projectA, 1);
        result3.addStage(createResultNode("stage2", 2, "test2", "agentx"));
        result3.addStage(createResultNode("stage3", 3, "test3", "agentx"));
        result3.addStage(createResultNode("stage4", 4, "test4", "agentx"));

        buildResultDao.save(result1);
        buildResultDao.save(result2);
        buildResultDao.save(result3);

        assertEquals(1, buildResultDao.getBuildCountByAgentName("agent1", null, null));
        assertEquals(2, buildResultDao.getBuildCountByAgentName("agent2", null, null));
        assertEquals(1, buildResultDao.getBuildCountByAgentName("agent2", null, new ResultState[]{ResultState.SUCCESS}));
        assertEquals(1, buildResultDao.getBuildCountByAgentName("agent2", new Project[]{projectA}, null));
        assertEquals(1, buildResultDao.getBuildCountByAgentName("agent2", new Project[]{projectA}, new ResultState[]{ResultState.SUCCESS}));
        assertEquals(1, buildResultDao.getBuildCountByAgentName("agent2", new Project[]{projectB}, null));
        assertEquals(0, buildResultDao.getBuildCountByAgentName("agent2", new Project[]{projectB}, new ResultState[]{ResultState.SUCCESS}));
        assertEquals(1, buildResultDao.getBuildCountByAgentName("agent3", null, null));
        assertEquals(1, buildResultDao.getBuildCountByAgentName("agentx", null, null));
        assertEquals(0, buildResultDao.getBuildCountByAgentName("agent4", null, null));
    }

    public void testFindByAgent()
    {
        BuildResult result1 = createCompletedBuild(projectA, 1);
        result1.addStage(createResultNode("stage1", 1, "test1", "agent1"));
        result1.addStage(createResultNode("stage2", 2, "test2", "agent2"));

        BuildResult result2 = createCompletedBuild(projectB, 1);
        result2.setState(ResultState.FAILURE);
        result2.addStage(createResultNode("stage2", 2, "test2", "agent2"));
        result2.addStage(createResultNode("stage3", 3, "test3", "agent3"));

        BuildResult result3 = createCompletedBuild(projectA, 1);
        result3.addStage(createResultNode("stage2", 2, "test2", "agentx"));
        result3.addStage(createResultNode("stage3", 3, "test3", "agentx"));
        result3.addStage(createResultNode("stage4", 4, "test4", "agentx"));

        buildResultDao.save(result1);
        buildResultDao.save(result2);
        buildResultDao.save(result3);

        List<BuildResult> found = buildResultDao.findLatestByAgentName("agent1", null, null, -1, -1);
        assertEquals(asList(result1), found);

        found = buildResultDao.findLatestByAgentName("agent2", null, null, -1, -1);
        assertEquals(asList(result2, result1), found);
        found = buildResultDao.findLatestByAgentName("agent2", null, null, 1, -1);
        assertEquals(asList(result1), found);
        found = buildResultDao.findLatestByAgentName("agent2", null, new ResultState[]{ResultState.SUCCESS}, -1, -1);
        assertEquals(asList(result1), found);
        found = buildResultDao.findLatestByAgentName("agent2", new Project[]{projectA}, null, -1, -1);
        assertEquals(asList(result1), found);
        found = buildResultDao.findLatestByAgentName("agent2", new Project[]{projectA}, new ResultState[]{ResultState.SUCCESS}, -1, -1);
        assertEquals(asList(result1), found);
        found = buildResultDao.findLatestByAgentName("agent2", new Project[]{projectB}, null, -1, -1);
        assertEquals(asList(result2), found);
        found = buildResultDao.findLatestByAgentName("agent2", new Project[]{projectB}, new ResultState[]{ResultState.SUCCESS}, -1, -1);
        assertEquals(0, found.size());
        found = buildResultDao.findLatestByAgentName("agent2", null, null, 0, 1);
        assertEquals(asList(result2), found);

        found = buildResultDao.findLatestByAgentName("agent3", null, null, -1, -1);
        assertEquals(asList(result2), found);

        found = buildResultDao.findLatestByAgentName("agentx", null, null, -1, -1);
        assertEquals(asList(result3), found);

        assertEquals(0, buildResultDao.findLatestByAgentName("agent4", null, null, -1, -1).size());
    }

    public void testFindByProjectAndMetabuildId()
    {
        BuildResult result = createResultWithRecipes();
        result.setMetaBuildId(123);
        buildResultDao.save(result);

        BuildResult found = buildResultDao.findByProjectAndMetabuildId(projectA, 123);
        assertEquals(result, found);

        assertNull(buildResultDao.findByProjectAndMetabuildId(projectA, 11223344L));
    }

    public void testFindByAfterBuild()
    {
        List<BuildResult> results = save(createCompletedBuild(projectA, 1),
                createCompletedBuild(projectA, 2),
                createFailedBuild(projectA, 3),
                createCompletedBuild(projectA, 4),
                createCompletedBuild(projectA, 5),
                createFailedBuild(projectA, 6),
                createCompletedBuild(projectA, 7),
                createCompletedBuild(projectA, 8));

        List<BuildResult> buildResults = buildResultDao.findByAfterBuild(results.get(0).getId(), 1, ResultState.SUCCESS);
        assertEquals(results.get(1), buildResults.get(0));

        buildResults = buildResultDao.findByAfterBuild(results.get(0).getId(), 2, ResultState.SUCCESS);
        assertEquals(results.get(1), buildResults.get(0));
        assertEquals(results.get(3), buildResults.get(1));

        buildResults = buildResultDao.findByAfterBuild(results.get(0).getId(), 15, ResultState.SUCCESS);
        assertEquals(5, buildResults.size());

        buildResults = buildResultDao.findByAfterBuild(results.get(0).getId(), 2, ResultState.FAILURE);
        assertEquals(results.get(2), buildResults.get(0));
        assertEquals(results.get(5), buildResults.get(1));
    }

    public void testFindByAfterBoundToSingleProject()
    {
        List<BuildResult> results = save(createCompletedBuild(projectA, 1),
                createCompletedBuild(projectB, 2),
                createCompletedBuild(projectB, 3));

        List<BuildResult> buildResults = buildResultDao.findByAfterBuild(results.get(0).getId(), 3, ResultState.SUCCESS);
        assertEquals(0, buildResults.size());
    }

    public void testFindByBeforeBuild()
    {
        List<BuildResult> results = save(createCompletedBuild(projectA, 1),
                createCompletedBuild(projectA, 2),
                createFailedBuild(projectA, 3),
                createCompletedBuild(projectA, 4),
                createCompletedBuild(projectA, 5),
                createFailedBuild(projectA, 6),
                createCompletedBuild(projectA, 7),
                createCompletedBuild(projectA, 8));

        List<BuildResult> buildResults = buildResultDao.findByBeforeBuild(results.get(7).getId(), 1, ResultState.SUCCESS);
        assertEquals(results.get(6), buildResults.get(0));

        buildResults = buildResultDao.findByBeforeBuild(results.get(7).getId(), 2, ResultState.SUCCESS);
        assertEquals(results.get(4), buildResults.get(0));
        assertEquals(results.get(6), buildResults.get(1));

        buildResults = buildResultDao.findByBeforeBuild(results.get(7).getId(), 15, ResultState.SUCCESS);
        assertEquals(5, buildResults.size());

        buildResults = buildResultDao.findByBeforeBuild(results.get(7).getId(), 2, ResultState.FAILURE);
        assertEquals(results.get(2), buildResults.get(0));
        assertEquals(results.get(5), buildResults.get(1));
    }

    public void testFindByLatestBuild()
    {
        List<BuildResult> results = save(createCompletedBuild(projectA, 1),
                createCompletedBuild(projectA, 2),
                createFailedBuild(projectA, 3),
                createCompletedBuild(projectA, 4));

        BuildResult buildResult = buildResultDao.findByLatestBuild(results.get(0).getId());
        assertEquals(results.get(3), buildResult);

        buildResult = buildResultDao.findByLatestBuild(results.get(0).getId(), ResultState.SUCCESS);
        assertEquals(results.get(3), buildResult);

        buildResult = buildResultDao.findByLatestBuild(results.get(0).getId(), ResultState.FAILURE);
        assertEquals(results.get(2), buildResult);
    }

    public void testFindCompletedSince()
    {
        User user = new User();
        userDao.save(user);

        buildResultDao.save(createCompletedBuild(projectA, 1));
        buildResultDao.save(createCompletedBuild(projectB, 1));
        long timeAfterBuild1 = time;
        buildResultDao.save(createCompletedBuild(projectA, 2));
        buildResultDao.save(createCompletedBuild(projectB, 2));
        buildResultDao.save(createCompletedBuild(projectA, 3));
        buildResultDao.save(createCompletedBuild(projectB, 3));
        long timeAfterBuild3 = time;
        buildResultDao.save(createCompletedBuild(projectA, 4));
        buildResultDao.save(createCompletedBuild(projectB, 4));
        buildResultDao.save(createIncompleteBuild(projectA, 5));
        buildResultDao.save(createIncompleteBuild(projectB, 5));

        buildResultDao.save(createPersonalBuild(user, projectA, 1));

        commitAndRefreshTransaction();

        List<BuildResult> results = buildResultDao.findCompletedSince(new Project[]{projectA}, -1);
        assertEquals(4, results.size());
        assertBuild(projectA, 4, results.get(0));
        assertBuild(projectA, 3, results.get(1));
        assertBuild(projectA, 2, results.get(2));
        assertBuild(projectA, 1, results.get(3));

        results = buildResultDao.findCompletedSince(new Project[]{projectA, projectB}, -1);
        assertEquals(8, results.size());
        assertBuild(projectB, 4, results.get(0));
        assertBuild(projectA, 4, results.get(1));
        assertBuild(projectB, 3, results.get(2));
        assertBuild(projectA, 3, results.get(3));
        assertBuild(projectB, 2, results.get(4));
        assertBuild(projectA, 2, results.get(5));
        assertBuild(projectB, 1, results.get(6));
        assertBuild(projectA, 1, results.get(7));

        results = buildResultDao.findCompletedSince(new Project[]{projectA}, timeAfterBuild1);
        assertEquals(3, results.size());
        assertBuild(projectA, 4, results.get(0));
        assertBuild(projectA, 3, results.get(1));
        assertBuild(projectA, 2, results.get(2));

        results = buildResultDao.findCompletedSince(new Project[]{projectA}, timeAfterBuild3);
        assertEquals(1, results.size());
        assertBuild(projectA, 4, results.get(0));

        results = buildResultDao.findCompletedSince(new Project[]{projectA}, time);
        assertEquals(0, results.size());
    }

    private List<BuildResult> save(BuildResult... results)
    {
        for (BuildResult result: results)
        {
            buildResultDao.save(result);
        }
        commitAndRefreshTransaction();
        return asList(results);
    }

    private void assertBuild(Project expectedProject, long expectedNumber, BuildResult build)
    {
        assertEquals(expectedProject, build.getProject());
        assertEquals(expectedNumber, build.getNumber());
    }

    private BuildResult createResultWithRecipes()
    {
        BuildResult result = createCompletedBuild(projectA, 1);
        result.addStage(createResultNode("stage1", 1, "test1", null));
        result.addStage(createResultNode("stage2", 2, "test2", null));
        return result;
    }

    private RecipeResultNode createResultNode(String stageName, long stageHandle, String recipeName, String hostName)
    {
        RecipeResultNode node = new RecipeResultNode(stageName, stageHandle, new RecipeResult(recipeName));
        node.setAgentName(hostName);
        return node;
    }

    private long getRecipeId(BuildResult result, int recipeIndex)
    {
        return result.getStages().get(recipeIndex).getResult().getId();
    }

    private void addMessageBuild(Project projectA, Feature.Level level, int number)
    {
        BuildResult result = createCompletedBuild(projectA, number);
        result.addFeature(level, "a message");
        result.calculateFeatureCounts();
        buildResultDao.save(result);
    }

    private BuildResult createIncompleteBuild(Project project, long number)
    {
        BuildResult result = new BuildResult(TEST_REASON, project, number, false);
        result.setRevision(new Revision(number));
        result.commence(time++);
        return result;
    }

    private BuildResult createCompletedBuild(Project project, long number)
    {
        BuildResult result = new BuildResult(TEST_REASON, project, number, false);
        result.setRevision(new Revision(number));
        result.commence(time++);
        result.complete(time++);
        return result;
    }

    private BuildResult createFailedBuild(Project project, long number)
    {
        BuildResult result = new BuildResult(TEST_REASON, project, number, false);
        result.setRevision(new Revision(number));
        result.commence(time++);
        result.failure();
        result.complete(time++);
        return result;
    }

    private BuildResult createPersonalBuild(User user, Project project, long number)
    {
        BuildResult result = createIncompletePersonalBuild(user, project, number);
        result.setRevision(new Revision(number));
        result.complete(time++);
        return result;
    }

    private BuildResult createIncompletePersonalBuild(User user, Project project, long number)
    {
        BuildResult result = new BuildResult(new PersonalBuildReason(user.getLogin()), user, project, number);
        result.setRevision(new Revision(number));
        result.commence(time++);
        return result;
    }
}
