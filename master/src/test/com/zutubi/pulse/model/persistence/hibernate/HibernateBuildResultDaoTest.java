package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.model.persistence.BuildResultDao;
import com.zutubi.pulse.model.persistence.BuildSpecificationDao;
import com.zutubi.pulse.model.persistence.ChangelistDao;
import com.zutubi.pulse.model.persistence.ProjectDao;

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

    public void setUp() throws Exception
    {
        super.setUp();
        buildResultDao = (BuildResultDao) context.getBean("buildResultDao");
        projectDao = (ProjectDao) context.getBean("projectDao");
        buildSpecificationDao = (BuildSpecificationDao) context.getBean("buildSpecificationDao");
        changelistDao = (ChangelistDao) context.getBean("changelistDao");
    }

    public void tearDown() throws Exception
    {
        buildSpecificationDao = null;
        projectDao = null;
        buildResultDao = null;
        changelistDao = null;

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

        List<BuildResult> latestCompleted = buildResultDao.findLatestCompleted(p1, b1.getName(), 10);
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

        List<BuildResult> latestCompleted = buildResultDao.findLatestCompleted(p1, b1.getName(), 10);
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

        List<BuildResult> latestCompleted = buildResultDao.findLatestCompleted(p1, b1.getName(), 10);
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

        List<BuildResult> latestCompleted = buildResultDao.findLatestCompleted(p1, b1.getName(), 2);
        assertEquals(2, latestCompleted.size());
        assertPropertyEquals(r4, latestCompleted.get(0));
        assertPropertyEquals(r3, latestCompleted.get(1));
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
}
