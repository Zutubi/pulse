package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.core2.BuildResult;
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
        BuildResult buildResult = new BuildResult("project");
        buildResult.setSucceeded(true);
        buildResult.setStamps(new TimeStamps(1, 2));
        buildResult.building();
        buildResultDao.save(buildResult);
        commitAndRefreshTransaction();
        
        BuildResult anotherBuildResult = buildResultDao.findById(buildResult.getId());
        
        // want to ensure that a new object has been created by hibernate and
        // that the old one is not just returned to us.
        assertFalse(buildResult == anotherBuildResult); 
        assertEquals(buildResult.getProjectName(), anotherBuildResult.getProjectName());
        assertEquals(buildResult.succeeded(), anotherBuildResult.succeeded());
        assertEquals(buildResult.getStamps(), anotherBuildResult.getStamps());
        assertEquals(BuildResult.BuildState.BUILDING, buildResult.getState());
    }
}
