package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.bootstrap.DatabaseBootstrap;
import junit.framework.TestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * 
 *
 */
public abstract class PersistenceTestCase extends TestCase
{
    protected ApplicationContext context = null;

    private PlatformTransactionManager transactionManager;
    private TransactionStatus transactionStatus;
    private DefaultTransactionDefinition transactionDefinition;

    public PersistenceTestCase()
    {

    }

    public PersistenceTestCase(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        String[] configLocations = new String[]{
            "com/cinnamonbob/bootstrap/testBootstrapContext.xml",
            "com/cinnamonbob/bootstrap/testApplicationContext.xml"
        };
        context = new ClassPathXmlApplicationContext(configLocations);

        DatabaseBootstrap dbBootstrap = new DatabaseBootstrap(context);
        dbBootstrap.initialiseDatabase();

        transactionManager = (PlatformTransactionManager)context.getBean("transactionManager");
        transactionDefinition = new DefaultTransactionDefinition(DefaultTransactionDefinition.PROPAGATION_REQUIRED);
        transactionStatus = transactionManager.getTransaction(transactionDefinition);
    }

    public void tearDown() throws Exception
    {
        transactionManager.commit(transactionStatus);
        Connection con = ((DataSource)context.getBean("dataSource")).getConnection();
        Statement stmt = null;
        try
        {
            stmt = con.createStatement();
            stmt.execute("SHUTDOWN");
        }
        finally
        {
            if (stmt != null)
                stmt.close();
        }

        super.tearDown();
    }

    protected void commitAndRefreshTransaction()
    {
        transactionManager.commit(transactionStatus);
        transactionStatus = transactionManager.getTransaction(transactionDefinition);
    }    
}
