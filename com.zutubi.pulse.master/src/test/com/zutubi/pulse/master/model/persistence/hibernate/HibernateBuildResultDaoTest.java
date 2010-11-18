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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * See also the BuildQueryTest.
 */
public class HibernateBuildResultDaoTest extends MasterPersistenceTestCase
{
    private static final TriggerBuildReason TEST_REASON = new TriggerBuildReason("scm trigger");

    private static long time = 0;

    private BuildResultDao buildResultDao;
    private ProjectDao projectDao;
    private ChangelistDao changelistDao;
    private UserDao userDao;

    private Project projectA;
    private Project projectB;

    public void setUp() throws Exception
    {
        super.setUp();
        buildResultDao = (BuildResultDao) context.getBean("buildResultDao");
        projectDao = (ProjectDao) context.getBean("projectDao");
        changelistDao = (ChangelistDao) context.getBean("changelistDao");
        userDao = (UserDao) context.getBean("userDao");
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

        // Need to save the Project as it is *not* cascaded from BuildResult
        Project project = new Project();
        projectDao.save(project);

        BuildResult buildResult = new BuildResult(TEST_REASON, project, 11, false);
        buildResult.commence();
        buildResult.setRevision(revision);
        RecipeResultNode recipeNode = new RecipeResultNode("stage name", 123, recipeResult);
        recipeNode.setHost("test host");
        buildResult.getRoot().addChild(recipeNode);

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
        result.complete();
        StoredFileArtifact artifact = new StoredFileArtifact("to file");
        PersistentPlainFeature feature = new PersistentPlainFeature(Feature.Level.ERROR, "getSummary here", 7);

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

        BuildResult result = new BuildResult(TEST_REASON, p1, 1, false);
        buildResultDao.save(result);

        commitAndRefreshTransaction();

        List<BuildResult> oldest = buildResultDao.findOldestByProject(p1, ResultState.getCompletedStates(), 1, false);
        assertEquals(0, oldest.size());
    }

    public void testGetOldestBuildsInProgress()
    {
        Project p1 = new Project();
        projectDao.save(p1);

        BuildResult result = new BuildResult(TEST_REASON, p1, 1, false);
        result.commence(0);
        buildResultDao.save(result);

        commitAndRefreshTransaction();

        List<BuildResult> oldest = buildResultDao.findOldestByProject(p1, ResultState.getCompletedStates(), 1, false);
        assertEquals(0, oldest.size());
    }

    public void testGetOldestBuildsExcludesPersonal()
    {
        User u1 = new User();
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
        User u1 = new User();
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

        BuildResult resultA = new BuildResult(TEST_REASON, p1, 1, false);
        buildResultDao.save(resultA);
        BuildResult resultB = new BuildResult(TEST_REASON, p1, 2, false);
        buildResultDao.save(resultB);
        BuildResult resultC = new BuildResult(TEST_REASON, p1, 3, false);
        buildResultDao.save(resultC);

        commitAndRefreshTransaction();

        assertNull(buildResultDao.findPreviousBuildResult(resultA));
        assertEquals(resultA, buildResultDao.findPreviousBuildResult(resultB));
        assertEquals(resultB, buildResultDao.findPreviousBuildResult(resultC));
    }

    public void testGetPreviousBuildResultWithRevision()
    {
        Project p1 = new Project();
        projectDao.save(p1);
        Project p2 = new Project();
        projectDao.save(p2);

        BuildResult r1 = createCompletedBuild(p1, 1);
        BuildResult r2 = createCompletedBuild(p1, 2);
        BuildResult r3 = createCompletedBuild(p1, 3);
        BuildResult other = createCompletedBuild(p2, 1);

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
        Project p1 = new Project();
        projectDao.save(p1);

        BuildResult r1 = createCompletedBuild(p1, 1);
        BuildResult r2 = createFailedBuild(p1, 2);
        BuildResult r3 = createCompletedBuild(p1, 3);

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
        Project p1 = new Project();
        projectDao.save(p1);

        BuildResult r1 = createUserRevisionBuild(p1, 1);
        BuildResult r2 = createCompletedBuild(p1, 2);
        BuildResult r3 = createUserRevisionBuild(p1, 3);
        BuildResult r4 = createCompletedBuild(p1, 4);

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
        Project p1 = new Project();
        projectDao.save(p1);

        BuildResult r1 = createNullRevisionBuild(p1, 1);
        BuildResult r2 = createCompletedBuild(p1, 2);
        BuildResult r3 = createNullRevisionBuild(p1, 3);
        BuildResult r4 = createCompletedBuild(p1, 4);

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
        Project p1 = new Project();
        projectDao.save(p1);

        BuildResult r1 = createPersonalBuild(u1, p1, 1);
        BuildResult r2 = createCompletedBuild(p1, 2);
        BuildResult r3 = createPersonalBuild(u1, p1, 3);
        BuildResult r4 = createCompletedBuild(p1, 4);

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
        Project p1 = new Project();
        Project p2 = new Project();
        projectDao.save(p1);
        projectDao.save(p2);

        BuildResult r1 = createCompletedBuild(p1, 1);
        BuildResult r2 = createCompletedBuild(p1, 2);
        BuildResult r3 = createCompletedBuild(p1, 3);
        BuildResult r4 = createCompletedBuild(p2, 3);

        buildResultDao.save(r1);
        buildResultDao.save(r2);
        buildResultDao.save(r3);
        buildResultDao.save(r4);

        commitAndRefreshTransaction();

        List<BuildResult> latestCompleted = buildResultDao.findLatestCompleted(p1, 0, 10);
        assertEquals(3, latestCompleted.size());
        assertPropertyEquals(r3, latestCompleted.get(0));
        assertPropertyEquals(r2, latestCompleted.get(1));
    }

    public void testGetLatestCompletedInitial()
    {
        Project p1 = new Project();
        projectDao.save(p1);

        BuildResult r1 = createCompletedBuild(p1, 1);
        BuildResult r2 = new BuildResult(TEST_REASON, p1, 2, false);

        buildResultDao.save(r1);
        buildResultDao.save(r2);

        commitAndRefreshTransaction();

        List<BuildResult> latestCompleted = buildResultDao.findLatestCompleted(p1, 0, 10);
        assertEquals(1, latestCompleted.size());
        assertPropertyEquals(r1, latestCompleted.get(0));
    }

    public void testGetLatestCompletedInProgress()
    {
        Project p1 = new Project();
        projectDao.save(p1);

        BuildResult r1 = createCompletedBuild(p1, 1);
        BuildResult r2 = new BuildResult(TEST_REASON, p1, 2, false);
        r2.commence();

        buildResultDao.save(r1);
        buildResultDao.save(r2);

        commitAndRefreshTransaction();

        List<BuildResult> latestCompleted = buildResultDao.findLatestCompleted(p1, 0, 10);
        assertEquals(1, latestCompleted.size());
        assertPropertyEquals(r1, latestCompleted.get(0));
    }

    public void testGetLatestCompletedMax()
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

        List<BuildResult> latestCompleted = buildResultDao.findLatestCompleted(p1, 0, 2);
        assertEquals(2, latestCompleted.size());
        assertPropertyEquals(r4, latestCompleted.get(0));
        assertPropertyEquals(r3, latestCompleted.get(1));
    }

    public void testGetLatestCompletedFirst()
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

        List<BuildResult> latestCompleted = buildResultDao.findLatestCompleted(p1, 1, 4);
        assertEquals(3, latestCompleted.size());
        assertPropertyEquals(r3, latestCompleted.get(0));
        assertPropertyEquals(r2, latestCompleted.get(1));
        assertPropertyEquals(r1, latestCompleted.get(2));
    }

    public void testDeleteBuildRetainChangelist()
    {
        Project p = new Project();
        projectDao.save(p);

        BuildResult result = createCompletedBuild(p, 1);
        result.setRevision(new Revision("10"));
        buildResultDao.save(result);

        PersistentChangelist list = new PersistentChangelist(new Revision("10"), 0, null, null, Collections.<PersistentFileChange>emptyList());
        list.setProjectId(p.getId());
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
        Project p = new Project();
        projectDao.save(p);

        User u1 = new User();
        User u2 = new User();
        User u3 = new User();
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
        Project p = new Project();
        projectDao.save(p);

        User u1 = new User();
        User u2 = new User();
        User u3 = new User();
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
        Project p = new Project();
        projectDao.save(p);

        User u1 = new User();
        User u2 = new User();
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
        Project p = new Project();
        projectDao.save(p);

        User u1 = new User();
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
        Project p = new Project();
        projectDao.save(p);

        User u1 = new User();
        User u2 = new User();
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
        Project p = new Project();
        projectDao.save(p);

        User u1 = new User();
        User u2 = new User();
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
        projectA = new Project();
        projectDao.save(projectA);

        projectB = new Project();
        projectDao.save(projectB);

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

    public void testFindLatestSuccessful()
    {
        createFindLatestSuccessfulTestData();

        BuildResult result = buildResultDao.findLatestSuccessful();
        assertEquals(4, result.getNumber());
    }

    public void testFindLatestSuccessfulByProject()
    {
        createFindLatestSuccessfulTestData();

        BuildResult result = buildResultDao.findLatestSuccessfulByProject(projectA);
        assertEquals(4, result.getNumber());

        result = buildResultDao.findLatestSuccessfulByProject(projectB);
        assertEquals(4, result.getNumber());
    }

    public void testGetBuildCountRange()
    {
        Project p1 = new Project();
        projectDao.save(p1);

        buildResultDao.save(createCompletedBuild(p1, 1));
        buildResultDao.save(createCompletedBuild(p1, 2));
        buildResultDao.save(createCompletedBuild(p1, 3));
        buildResultDao.save(createCompletedBuild(p1, 4));
        buildResultDao.save(createCompletedBuild(p1, 5));

        commitAndRefreshTransaction();

        assertEquals(0, buildResultDao.getBuildCount(p1, 1, 1));
        assertEquals(1, buildResultDao.getBuildCount(p1, 0, 1));
        assertEquals(2, buildResultDao.getBuildCount(p1, 0, 2));
        assertEquals(3, buildResultDao.getBuildCount(p1, 0, 3));
        assertEquals(2, buildResultDao.getBuildCount(p1, 1, 3));
        assertEquals(3, buildResultDao.getBuildCount(p1, 1, 4));
        assertEquals(4, buildResultDao.getBuildCount(p1, 1, 100));
        assertEquals(5, buildResultDao.getBuildCount(p1, 0, 100));
    }

    public void testQueryBuilds()
    {
        Project p1 = new Project();
        projectDao.save(p1);

        buildResultDao.save(createCompletedBuild(p1, 1));
        buildResultDao.save(createCompletedBuild(p1, 2));
        buildResultDao.save(createCompletedBuild(p1, 3));
        buildResultDao.save(createCompletedBuild(p1, 4));

        commitAndRefreshTransaction();

        List<BuildResult> results = buildResultDao.queryBuilds(p1, null, 1, -1, 0, 1, false, false);
        assertEquals(1, results.size());
        assertEquals(1, results.get(0).getNumber());

        results = buildResultDao.queryBuilds(p1, null, 2, -1, 0, 1, false, false);
        assertEquals(1, results.size());
        assertEquals(2, results.get(0).getNumber());

        results = buildResultDao.queryBuilds(p1, null, 3, -1, 0, 1, false, false);
        assertEquals(1, results.size());
        assertEquals(3, results.get(0).getNumber());

        results = buildResultDao.queryBuilds(p1, null, 4, -1, 0, 1, false, false);
        assertEquals(1, results.size());
        assertEquals(4, results.get(0).getNumber());
    }

    public void testQueryBuildsSuccess()
    {
        Project p1 = new Project();
        projectDao.save(p1);

        buildResultDao.save(createCompletedBuild(p1, 1));
        buildResultDao.save(createCompletedBuild(p1, 2));
        buildResultDao.save(createFailedBuild(p1, 3));
        buildResultDao.save(createCompletedBuild(p1, 4));
        buildResultDao.save(createCompletedBuild(p1, 5));
        buildResultDao.save(createFailedBuild(p1, 6));
        buildResultDao.save(createCompletedBuild(p1, 7));

        commitAndRefreshTransaction();
        
        List<BuildResult> results = buildResultDao.queryBuilds(p1, new ResultState[]{ ResultState.SUCCESS }, -1, 1, 0, 1, true, false);
        assertEquals(1, results.size());
        assertEquals(1, results.get(0).getNumber());

        results = buildResultDao.queryBuilds(p1, new ResultState[]{ ResultState.SUCCESS }, -1, 2, 0, 1, true, false);
        assertEquals(1, results.size());
        assertEquals(2, results.get(0).getNumber());

        results = buildResultDao.queryBuilds(p1, new ResultState[]{ ResultState.SUCCESS }, -1, 3, 0, 1, true, false);
        assertEquals(1, results.size());
        assertEquals(2, results.get(0).getNumber());

        results = buildResultDao.queryBuilds(p1, new ResultState[]{ ResultState.SUCCESS }, -1, 4, 0, 1, true, false);
        assertEquals(1, results.size());
        assertEquals(4, results.get(0).getNumber());

        results = buildResultDao.queryBuilds(p1, new ResultState[]{ ResultState.SUCCESS }, -1, 5, 0, 1, true, false);
        assertEquals(1, results.size());
        assertEquals(5, results.get(0).getNumber());

        results = buildResultDao.queryBuilds(p1, new ResultState[]{ ResultState.SUCCESS }, -1, 6, 0, 1, true, false);
        assertEquals(1, results.size());
        assertEquals(5, results.get(0).getNumber());

        results = buildResultDao.queryBuilds(p1, new ResultState[]{ ResultState.SUCCESS }, -1, 7, 0, 1, true, false);
        assertEquals(1, results.size());
        assertEquals(7, results.get(0).getNumber());
    }

    public void testQueryBuildsWithMessagesWarnings()
    {
        Project p1 = new Project();
        projectDao.save(p1);

        addMessageBuild(p1, Feature.Level.WARNING, 1);

        List<BuildResult> results = buildResultDao.queryBuildsWithMessages(new Project[]{p1}, Feature.Level.WARNING, 1);
        assertEquals(1, results.size());
        assertEquals(1, results.get(0).getNumber());

        results = buildResultDao.queryBuildsWithMessages(new Project[]{p1}, Feature.Level.ERROR, 1);
        assertEquals(0, results.size());
    }

    public void testQueryBuildsWithMessagesErrors()
    {
        Project p1 = new Project();
        projectDao.save(p1);

        addMessageBuild(p1, Feature.Level.ERROR, 1);

        List<BuildResult> results = buildResultDao.queryBuildsWithMessages(new Project[]{p1}, Feature.Level.ERROR, 1);
        assertEquals(1, results.size());
        assertEquals(1, results.get(0).getNumber());

        results = buildResultDao.queryBuildsWithMessages(new Project[]{p1}, Feature.Level.WARNING, 1);
        assertEquals(0, results.size());
    }

    public void testQueryBuildsWithMessagesMultiple()
    {
        Project p1 = new Project();
        projectDao.save(p1);

        Project p2 = new Project();
        projectDao.save(p2);

        addMessageBuild(p1, Feature.Level.ERROR, 1);
        addMessageBuild(p1, Feature.Level.ERROR, 2);
        addMessageBuild(p2, Feature.Level.WARNING, 1);
        addMessageBuild(p2, Feature.Level.ERROR, 2);
        addMessageBuild(p2, Feature.Level.WARNING, 3);

        List<BuildResult> results = buildResultDao.queryBuildsWithMessages(new Project[]{p1, p2}, Feature.Level.ERROR, 10);
        assertEquals(3, results.size());
        assertEquals(2, results.get(0).getNumber());
        assertEquals(p2, results.get(0).getProject());
        assertEquals(2, results.get(1).getNumber());
        assertEquals(p1, results.get(1).getProject());
        assertEquals(1, results.get(2).getNumber());
        assertEquals(p1, results.get(2).getProject());

        results = buildResultDao.queryBuildsWithMessages(new Project[]{p1, p2}, Feature.Level.ERROR, 1);
        assertEquals(1, results.size());
        assertEquals(2, results.get(0).getNumber());
        assertEquals(p2, results.get(0).getProject());

        results = buildResultDao.queryBuildsWithMessages(new Project[]{p2}, Feature.Level.WARNING, 10);
        assertEquals(2, results.size());
        assertEquals(3, results.get(0).getNumber());
        assertEquals(p2, results.get(0).getProject());
        assertEquals(1, results.get(1).getNumber());
        assertEquals(p2, results.get(1).getProject());
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
        Project project = new Project();
        projectDao.save(project);

        List<BuildResult> results = save(createCompletedBuild(project, 1),
                createCompletedBuild(project, 2),
                createFailedBuild(project, 3),
                createCompletedBuild(project, 4),
                createCompletedBuild(project, 5),
                createFailedBuild(project, 6),
                createCompletedBuild(project, 7),
                createCompletedBuild(project, 8));

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
        Project projectA = new Project();
        projectDao.save(projectA);
        Project projectB = new Project();
        projectDao.save(projectB);

        List<BuildResult> results = save(createCompletedBuild(projectA, 1),
                createCompletedBuild(projectB, 2),
                createCompletedBuild(projectB, 3));

        List<BuildResult> buildResults = buildResultDao.findByAfterBuild(results.get(0).getId(), 3, ResultState.SUCCESS);
        assertEquals(0, buildResults.size());
    }

    public void testFindByBeforeBuild()
    {
        Project project = new Project();
        projectDao.save(project);

        List<BuildResult> results = save(createCompletedBuild(project, 1),
                createCompletedBuild(project, 2),
                createFailedBuild(project, 3),
                createCompletedBuild(project, 4),
                createCompletedBuild(project, 5),
                createFailedBuild(project, 6),
                createCompletedBuild(project, 7),
                createCompletedBuild(project, 8));

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
        Project project = new Project();
        projectDao.save(project);

        List<BuildResult> results = save(createCompletedBuild(project, 1),
                createCompletedBuild(project, 2),
                createFailedBuild(project, 3),
                createCompletedBuild(project, 4));

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

        Project p1 = new Project();
        Project p2 = new Project();
        projectDao.save(p1);
        projectDao.save(p2);

        buildResultDao.save(createCompletedBuild(p1, 1));
        buildResultDao.save(createCompletedBuild(p2, 1));
        long timeAfterBuild1 = time;
        buildResultDao.save(createCompletedBuild(p1, 2));
        buildResultDao.save(createCompletedBuild(p2, 2));
        buildResultDao.save(createCompletedBuild(p1, 3));
        buildResultDao.save(createCompletedBuild(p2, 3));
        long timeAfterBuild3 = time;
        buildResultDao.save(createCompletedBuild(p1, 4));
        buildResultDao.save(createCompletedBuild(p2, 4));
        buildResultDao.save(createIncompleteBuild(p1, 5));
        buildResultDao.save(createIncompleteBuild(p2, 5));

        buildResultDao.save(createPersonalBuild(user, p1, 1));

        commitAndRefreshTransaction();

        List<BuildResult> results = buildResultDao.findCompletedSince(new Project[]{p1}, -1);
        assertEquals(4, results.size());
        assertBuild(p1, 4, results.get(0));
        assertBuild(p1, 3, results.get(1));
        assertBuild(p1, 2, results.get(2));
        assertBuild(p1, 1, results.get(3));

        results = buildResultDao.findCompletedSince(new Project[]{p1, p2}, -1);
        assertEquals(8, results.size());
        assertBuild(p2, 4, results.get(0));
        assertBuild(p1, 4, results.get(1));
        assertBuild(p2, 3, results.get(2));
        assertBuild(p1, 3, results.get(3));
        assertBuild(p2, 2, results.get(4));
        assertBuild(p1, 2, results.get(5));
        assertBuild(p2, 1, results.get(6));
        assertBuild(p1, 1, results.get(7));

        results = buildResultDao.findCompletedSince(new Project[]{p1}, timeAfterBuild1);
        assertEquals(3, results.size());
        assertBuild(p1, 4, results.get(0));
        assertBuild(p1, 3, results.get(1));
        assertBuild(p1, 2, results.get(2));

        results = buildResultDao.findCompletedSince(new Project[]{p1}, timeAfterBuild3);
        assertEquals(1, results.size());
        assertBuild(p1, 4, results.get(0));

        results = buildResultDao.findCompletedSince(new Project[]{p1}, time);
        assertEquals(0, results.size());
    }

    private List<BuildResult> save(BuildResult... results)
    {
        for (BuildResult result: results)
        {
            buildResultDao.save(result);
        }
        commitAndRefreshTransaction();
        return Arrays.asList(results);
    }

    private void assertBuild(Project expectedProject, long expectedNumber, BuildResult build)
    {
        assertEquals(expectedProject, build.getProject());
        assertEquals(expectedNumber, build.getNumber());
    }

    private BuildResult createResultWithRecipes()
    {
        BuildResult result = createCompletedBuild(projectA, 1);
        result.getRoot().addChild(new RecipeResultNode("stage1", 1, new RecipeResult("test1")));
        result.getRoot().addChild(new RecipeResultNode("stage2", 2, new RecipeResult("test2")));
        return result;
    }

    private long getRecipeId(BuildResult result, int recipeIndex)
    {
        return result.getRoot().getChildren().get(recipeIndex).getResult().getId();
    }

    private void addMessageBuild(Project p1, Feature.Level level, int number)
    {
        BuildResult result = createCompletedBuild(p1, number);
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
