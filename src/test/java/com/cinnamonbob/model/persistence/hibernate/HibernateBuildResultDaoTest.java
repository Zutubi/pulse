package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.bootstrap.DBBootstrap;
import com.cinnamonbob.core2.BuildResult;
import com.cinnamonbob.model.persistence.BuildResultDao;
import com.cinnamonbob.util.TimeStamps;

import junit.framework.TestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;


/**
 * 
 *
 * @noinspection FieldCanBeLocal
 */
public class HibernateBuildResultDaoTest extends TestCase
{
    private ApplicationContext context = null;
    
    private BuildResultDao buildResultDao;
    
    private PlatformTransactionManager transactionManager;
    private TransactionStatus transactionStatus;
    private DefaultTransactionDefinition transactionDefinition;
    
    public void setUp() throws Exception
    {
        String[] configLocations = new String[]{
            "com/cinnamonbob/bootstrap/testApplicationContext.xml"
        };
        context = new ClassPathXmlApplicationContext(configLocations);
        buildResultDao = (BuildResultDao) context.getBean("buildResultDao");
        
        DBBootstrap dbBootstrap = new DBBootstrap(context);
        dbBootstrap.initialiseDatabase();

        transactionManager = (PlatformTransactionManager)context.getBean("transactionManager");
        transactionDefinition = new DefaultTransactionDefinition(DefaultTransactionDefinition.PROPAGATION_REQUIRED);
        transactionStatus = transactionManager.getTransaction(transactionDefinition);        
    }
    
    public void tearDown() throws Exception
    {
        transactionManager.commit(transactionStatus);        
        context = null;
    }
    
    public void testSaveAndLoad()
    {
        BuildResult buildResult = new BuildResult();
        buildResult.setProjectName("project");
        buildResult.setSucceeded(true);
        buildResult.setStamps(new TimeStamps(1, 2));
        buildResult.building();
        buildResultDao.save(buildResult);
        commitAndRefreshTransaction();
        
        BuildResult anotherBuildResult = (BuildResult) buildResultDao.findById(buildResult.getId());
        
        // want to ensure that a new object has been created by hibernate and
        // that the old one is not just returned to us.
        assertFalse(buildResult == anotherBuildResult); 
        assertEquals(buildResult.getProjectName(), anotherBuildResult.getProjectName());
        assertEquals(buildResult.succeeded(), anotherBuildResult.succeeded());
        assertEquals(buildResult.getStamps(), anotherBuildResult.getStamps());
        assertEquals(BuildResult.BuildState.BUILDING, buildResult.getState());
    }
    
    protected void commitAndRefreshTransaction()
    {
        transactionManager.commit(transactionStatus);
        transactionStatus = transactionManager.getTransaction(transactionDefinition);
    }
}
