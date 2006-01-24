package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.core.FileArtifact;
import com.cinnamonbob.core.model.*;
import com.cinnamonbob.model.BuildResult;
import com.cinnamonbob.model.BuildScmDetails;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.RecipeResultNode;
import com.cinnamonbob.model.persistence.BuildResultDao;
import com.cinnamonbob.model.persistence.ProjectDao;

import java.io.File;
import java.util.Calendar;
import java.util.List;


/**
 * 
 *
 */
public class HibernateBuildResultDaoTest extends MasterPersistenceTestCase
{
    private BuildResultDao buildResultDao;
    private ProjectDao projectDao;

    public void setUp() throws Exception
    {
        super.setUp();
        buildResultDao = (BuildResultDao) context.getBean("buildResultDao");
        projectDao = (ProjectDao) context.getBean("projectDao");
    }

    public void tearDown() throws Exception
    {
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

        BuildScmDetails scmDetails = new BuildScmDetails(new NumericalRevision(42), null);

        Revision revision = new NumericalRevision(12345);
        revision.setDate(Calendar.getInstance().getTime());
        revision.setAuthor("user");
        revision.setComment("i like fruit");

        Changelist changes = new Changelist(revision);
        changes.addChange(new Change("/filename.1", "1.0", Change.Action.ADD));
        changes.addChange(new Change("/filename.2", "2.0", Change.Action.DELETE));
        changes.addChange(new Change("/filename.3", "3.0", Change.Action.EDIT));

        scmDetails.add(changes);

        // Need to save the Project as it is *not* cascaded from BuildResult
        Project project = new Project();
        projectDao.save(project);

        BuildResult buildResult = new BuildResult(project, 11);
        buildResult.commence(new File("/tmp/buildout"));
        buildResult.setScmDetails(scmDetails);
        RecipeResultNode recipeNode = new RecipeResultNode(recipeResult);
        recipeNode.setHost("test host");
        buildResult.getRoot().addChild(recipeNode);

        buildResultDao.save(buildResult);
        commitAndRefreshTransaction();

        BuildResult anotherBuildResult = buildResultDao.findById(buildResult.getId());
        assertPropertyEquals(buildResult, anotherBuildResult);
    }

    private RecipeResult createRecipe()
    {
        RecipeResult recipeResult = new RecipeResult("project");
        recipeResult.commence(new File("/tmp/buildout"));
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
        recipeResult.commence(new File("/tmp/buildout"));
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
        result.commence(new File("/tmp/commandout"));
        result.error("woops!");
        return result;
    }

    private CommandResult createFailedCommand()
    {
        CommandResult result;
        result = new CommandResult("command name");
        result.commence(new File("/tmp/commandout"));
        result.failure("oh no!");
        return result;
    }

    private CommandResult createArtifactCommand()
    {
        FileArtifact fa = new FileArtifact("file artifact", new File("/tmp/foo"));
        fa.setTitle("title");
        fa.setType("type");

        CommandResult result = new CommandResult("command name");
        result.commence(new File("/tmp/commandout"));
        result.success();
        StoredArtifact artifact = new StoredArtifact(fa, "to file");
        PlainFeature feature = new PlainFeature(Feature.Level.ERROR, "getSummary here", 7);

        artifact.addFeature(feature);
        result.addArtifact(artifact);
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

        BuildResult result = new BuildResult(p1, 1);
        buildResultDao.save(result);

        commitAndRefreshTransaction();

        List<BuildResult> oldest = buildResultDao.findOldestByProject(p1, 1);
        assertEquals(0, oldest.size());
    }

    public void testGetOldestBuildsInProgress()
    {
        Project p1 = new Project();
        projectDao.save(p1);

        BuildResult result = new BuildResult(p1, 1);
        result.commence(0);
        buildResultDao.save(result);

        commitAndRefreshTransaction();

        List<BuildResult> oldest = buildResultDao.findOldestByProject(p1, 1);
        assertEquals(0, oldest.size());
    }

    private BuildResult createCompletedBuild(Project project, long number)
    {
        BuildResult result = new BuildResult(project, number);
        result.commence(0);
        result.complete();
        return result;
    }

}
