package com.cinnamonbob.model.persistence.hibernate;

import java.io.File;

import com.cinnamonbob.core.BuildResult;
import com.cinnamonbob.core.config.FileArtifact;
import com.cinnamonbob.model.CommandResult;
import com.cinnamonbob.model.Feature;
import com.cinnamonbob.model.PlainFeature;
import com.cinnamonbob.model.StoredArtifact;
import com.cinnamonbob.model.persistence.BuildResultDao;
import com.cinnamonbob.util.TimeStamps;


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
        super.tearDown();
    }
    
    public void testSaveAndLoad()
    {
        FileArtifact fa = new FileArtifact("file artifact", new File("/tmp/foo"));
        fa.setTitle("title");
        fa.setType("type");
        
        CommandResult result = new CommandResult();
        result.setCommandName("command name");
        result.setStamps(new TimeStamps(3, 4));
        result.setSucceeded(true);
        StoredArtifact artifact = new StoredArtifact(fa, "to file");
        PlainFeature feature = new PlainFeature(Feature.Level.ERROR, "summary here", 7);
        
        artifact.addFeature(feature);
        result.addArtifact(artifact);
        
        BuildResult buildResult = new BuildResult("project", 11);
        buildResult.setSucceeded(true);
        buildResult.setStamps(new TimeStamps(1, 2));
        buildResult.building();
        buildResult.setRevision("42");
        buildResult.add(result);
        
        buildResultDao.save(buildResult);
        commitAndRefreshTransaction();
        
        BuildResult anotherBuildResult = buildResultDao.findById(buildResult.getId());
        
        // want to ensure that a new object has been created by hibernate and
        // that the old one is not just returned to us.
        assertFalse(buildResult == anotherBuildResult); 
        assertEquals(buildResult.getNumber(), anotherBuildResult.getNumber());
        assertEquals(buildResult.getProjectName(), anotherBuildResult.getProjectName());
        assertEquals(buildResult.succeeded(), anotherBuildResult.succeeded());
        assertEquals(buildResult.getStamps(), anotherBuildResult.getStamps());
        assertEquals(buildResult.getRevision(), anotherBuildResult.getRevision());
        assertEquals(BuildResult.BuildState.BUILDING, buildResult.getState());
        assertEquals(anotherBuildResult.getCommandResults().size(), 1);
        
        // TODO seems kinda lame not to use equals() on the objects?
        CommandResult anotherResult = anotherBuildResult.getCommandResults().get(0);
        assertEquals(result.getCommandName(), anotherResult.getCommandName());
        assertEquals(result.getStamps(), anotherResult.getStamps());
        assertEquals(result.succeeded(), anotherResult.succeeded());
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
