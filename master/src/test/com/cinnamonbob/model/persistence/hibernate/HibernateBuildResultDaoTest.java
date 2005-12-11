package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.core.FileArtifact;
import com.cinnamonbob.core.model.*;
import com.cinnamonbob.model.*;
import com.cinnamonbob.model.persistence.BuildResultDao;

import java.io.File;
import java.util.Calendar;


/**
 * 
 *
 */
public class HibernateBuildResultDaoTest extends PersistenceTestCase
{
    private BuildResultDao buildResultDao;

    public void setUp() throws Exception
    {
        super.setUp();
        buildResultDao = (BuildResultDao) context.getBean("buildResultDao");
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

    public void testSaveAndLoad()
    {
        FileArtifact fa = new FileArtifact("file artifact", new File("/tmp/foo"));
        fa.setTitle("title");
        fa.setType("type");

        CommandResult result = new CommandResult("command name");
        result.commence(new File("/tmp/commandout"));
        result.success();
        StoredArtifact artifact = new StoredArtifact(fa, "to file");
        PlainFeature feature = new PlainFeature(Feature.Level.ERROR, "summary here", 7);

        artifact.addFeature(feature);
        result.addArtifact(artifact);

        RecipeResult recipeResult = new RecipeResult("project");
        recipeResult.commence(new File("/tmp/buildout"));
        recipeResult.complete();
        recipeResult.add(result);

        BuildScmDetails scmDetails = new BuildScmDetails("my scm", new NumericalRevision(42), null);

        Revision revision = new NumericalRevision(12345);
        revision.setDate(Calendar.getInstance().getTime());
        revision.setAuthor("user");
        revision.setComment("i like fruit");

        Changelist changes = new Changelist(revision);
        changes.addChange(new Change("/filename.1", "1.0", Change.Action.ADD));
        changes.addChange(new Change("/filename.2", "2.0", Change.Action.DELETE));
        changes.addChange(new Change("/filename.3", "3.0", Change.Action.EDIT));

        scmDetails.add(changes);

        BuildResult buildResult = new BuildResult(new Project(), 11);
        buildResult.commence(new File("/tmp/buildout"));
        buildResult.addScmDetails(1, scmDetails);
        buildResult.add(new RecipeResultNode("my node", recipeResult));

        buildResultDao.save(buildResult);
        commitAndRefreshTransaction();

        BuildResult anotherBuildResult = buildResultDao.findById(buildResult.getId());
        assertPropertyEquals(buildResult, anotherBuildResult);
    }
}
