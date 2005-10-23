package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.bootstrap.ComponentContext;
import com.cinnamonbob.bootstrap.DatabaseBootstrap;
import junit.framework.TestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.EntityMode;
import org.hibernate.SessionFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;

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

    protected SessionFactory sessionFactory;

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

        ComponentContext.addClassPathContextDefinitions(configLocations);
        context = ComponentContext.getContext();

        DatabaseBootstrap dbBootstrap = new DatabaseBootstrap();
        dbBootstrap.initialiseDatabase();

        transactionManager = (PlatformTransactionManager)context.getBean("transactionManager");
        transactionDefinition = new DefaultTransactionDefinition(DefaultTransactionDefinition.PROPAGATION_REQUIRED);
        transactionStatus = transactionManager.getTransaction(transactionDefinition);

        sessionFactory = (SessionFactory) context.getBean("sessionFactory");
    }

    public void tearDown() throws Exception
    {
        sessionFactory = null;

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
        transactionStatus = null;
        transactionDefinition = null;
        transactionManager = null;
        super.tearDown();
    }

    protected void commitAndRefreshTransaction()
    {
        transactionManager.commit(transactionStatus);
        transactionStatus = transactionManager.getTransaction(transactionDefinition);
    }

    protected void assertPersistentEquals(Object a, Object b)
    {
        SessionFactoryImplementor imp = (SessionFactoryImplementor) sessionFactory;
        ClassMetadata metadata = imp.getClassMetadata(a.getClass());
        EntityPersister persister = imp.getEntityPersister(metadata.getEntityName());
        String[] properties = metadata.getPropertyNames();
        for (String propertyName : properties)
        {
            Object valueA = persister.getPropertyValue(a, propertyName, EntityMode.POJO);
            Object valueB = persister.getPropertyValue(b, propertyName, EntityMode.POJO);
            assertEquals(propertyName, valueA, valueB);
        }
    }

    protected void assertEquals(Map a, Map b)
    {
        if (a == null)
        {
            assertNull(b);
        }
        else
        {
            assertNotNull(b);
        }
        assertEquals(a.size(), b.size());
        for (Object key : a.keySet())
        {
            assertEquals(key.toString(), a.get(key), b.get(key));
        }
    }
}
