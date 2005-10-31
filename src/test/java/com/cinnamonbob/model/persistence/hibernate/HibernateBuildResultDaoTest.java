package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.core.FileArtifact;
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

        BuildResult buildResult = new BuildResult("project", 11);
        buildResult.commence(new File("/tmp/buildout"));
        buildResult.complete();
        buildResult.add(result);

        BuildScmDetails scmDetails = new BuildScmDetails();
        buildResult.addScmDetails(1, scmDetails);
        scmDetails.setRevision(new NumericalRevision(42));

        Revision revision = new NumericalRevision(12345);
        revision.setDate(Calendar.getInstance().getTime());
        revision.setAuthor("user");
        revision.setComment("i like fruit");
        
        Changelist changes = new Changelist(revision);
        changes.addChange(new Change("/filename.1", "1.0", Change.Action.ADD));
        changes.addChange(new Change("/filename.2", "2.0", Change.Action.DELETE));
        changes.addChange(new Change("/filename.3", "3.0", Change.Action.EDIT));

        scmDetails.add(changes);

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

        assertEquals(buildResult.getScmDetails().size(), 1);
        BuildScmDetails anotherScmDetails = anotherBuildResult.getScmDetails(1);
        assertNotNull(anotherScmDetails);
        assertEquals(scmDetails.getRevision(), anotherScmDetails.getRevision());
        assertEquals(1, anotherScmDetails.getChangelists().size());

        Changelist otherChanges = anotherScmDetails.getChangelists().get(0);
        assertEquals(3, otherChanges.getChanges().size());

        assertEquals(anotherBuildResult.getCommandResults().size(), 1);
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

    }
}
