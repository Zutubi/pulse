package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.core.config.FileArtifact;
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

        CommandResult result = new CommandResult();
        result.setCommandName("command name");
        result.commence();
        result.success();
        StoredArtifact artifact = new StoredArtifact(fa, "to file");
        PlainFeature feature = new PlainFeature(Feature.Level.ERROR, "summary here", 7);

        artifact.addFeature(feature);
        result.addArtifact(artifact);

        BuildResult buildResult = new BuildResult("project", 11);
        buildResult.commence();
        buildResult.complete();
        buildResult.setRevision(new NumericalRevision(42));
        buildResult.add(result);

        Revision revision = new NumericalRevision(12345);
        Changelist changes = new Changelist(revision, Calendar.getInstance().getTime(), "user", "i like fruit");
        changes.addChange(new Change("/filename.1", "1.0", Change.Action.ADD));
        changes.addChange(new Change("/filename.2", "2.0", Change.Action.DELETE));
        changes.addChange(new Change("/filename.3", "3.0", Change.Action.EDIT));

        buildResult.add(changes);

        buildResultDao.save(buildResult);
        commitAndRefreshTransaction();

        BuildResult anotherBuildResult = buildResultDao.findById(buildResult.getId());

        // want to ensure that a new object has been created by hibernate and
        // that the old one is not just returned to us.
        assertFalse(buildResult == anotherBuildResult);
        assertEquals(buildResult.getNumber(), anotherBuildResult.getNumber());
        assertEquals(buildResult.getProjectName(), anotherBuildResult.getProjectName());
        assertEquals(buildResult.getState(), anotherBuildResult.getState());
        assertEquals(buildResult.getStamps(), anotherBuildResult.getStamps());
        assertEquals(buildResult.getRevision(), anotherBuildResult.getRevision());
        assertEquals(anotherBuildResult.getCommandResults().size(), 1);

        // TODO seems kinda lame not to use equals() on the objects?
        CommandResult anotherResult = anotherBuildResult.getCommandResults().get(0);
        assertEquals(result.getCommandName(), anotherResult.getCommandName());
        assertEquals(result.getStamps(), anotherResult.getStamps());
        assertEquals(result.getState(), anotherResult.getState());
        assertEquals(anotherResult.getArtifacts().size(), 1);

        StoredArtifact anotherArtifact = anotherResult.getArtifacts().get(0);
        assertEquals(artifact.getName(), anotherArtifact.getName());
        assertEquals(artifact.getTitle(), anotherArtifact.getTitle());
        assertEquals(artifact.getType(), anotherArtifact.getType());
        assertEquals(artifact.getFile(), anotherArtifact.getFile());
        assertEquals(artifact.getFeatures(artifact.getLevels().next()).size(), 1);

        Feature otherFeature = artifact.getFeatures(artifact.getLevels().next()).get(0);
        assertTrue(otherFeature instanceof PlainFeature);
        PlainFeature otherPlain = (PlainFeature)otherFeature;
        assertEquals(feature.getLevel(), otherPlain.getLevel());
        assertEquals(feature.getSummary(), otherPlain.getSummary());
        assertEquals(feature.getLineNumber(), otherPlain.getLineNumber());

        assertEquals(1, anotherBuildResult.getChangelists().size());

        Changelist otherChanges = anotherBuildResult.getChangelists().get(0);
        assertEquals(3, otherChanges.getChanges().size());
    }
}
