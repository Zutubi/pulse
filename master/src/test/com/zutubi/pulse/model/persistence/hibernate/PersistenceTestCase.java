package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.bootstrap.DatabaseConsole;
import com.zutubi.pulse.test.PulseTestCase;
import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * 
 *
 */
public abstract class PersistenceTestCase extends PulseTestCase
{
    protected ApplicationContext context = null;

    private PlatformTransactionManager transactionManager;
    private TransactionStatus transactionStatus;
    private DefaultTransactionDefinition transactionDefinition;

    protected SessionFactory sessionFactory;
    protected DataSource dataSource;
    protected DatabaseConsole console;

    public PersistenceTestCase()
    {

    }

    public PersistenceTestCase(String testName)
    {
        super(testName);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        String[] configLocations = getConfigLocations();

        ComponentContext.addClassPathContextDefinitions(configLocations);
        context = ComponentContext.getContext();

        dataSource = (DataSource) context.getBean("dataSource");
        console = (DatabaseConsole) context.getBean("databaseConsole");

        if (console.schemaExists())
        {
            console.dropSchema();
        }
        console.createSchema();

        transactionManager = (PlatformTransactionManager) context.getBean("transactionManager");
        transactionDefinition = new DefaultTransactionDefinition(DefaultTransactionDefinition.PROPAGATION_REQUIRED);
        transactionStatus = transactionManager.getTransaction(transactionDefinition);

        sessionFactory = (SessionFactory) context.getBean("sessionFactory");
    }

    protected void tearDown() throws Exception
    {
        sessionFactory.close();
        sessionFactory = null;
        try
        {
            transactionManager.commit(transactionStatus);
        }
        catch (Exception e)
        {
            // catch the exception and continue.
            e.printStackTrace();
        }

        console.stop(false);

        dataSource = null;
        console = null;
        transactionStatus = null;
        transactionDefinition = null;
        transactionManager = null;
        ComponentContext.closeAll();
        context = null;
        
        super.tearDown();
    }

    protected abstract String[] getConfigLocations();

    protected void commitAndRefreshTransaction()
    {
        transactionManager.commit(transactionStatus);
        transactionStatus = transactionManager.getTransaction(transactionDefinition);
    }

    protected void assertPropertyEquals(Object a, Object b)
    {
        if(a == null)
        {
            assertNull(b);
        }
        else
        {
            assertNotNull(b);
        }
        
        BeanInfo beanInfo = null;
        try
        {
            beanInfo = Introspector.getBeanInfo(a.getClass());
        }
        catch (IntrospectionException e)
        {
            throw new RuntimeException(e);
        }

        for (PropertyDescriptor property : beanInfo.getPropertyDescriptors())
        {
            Method getter = property.getReadMethod();
            if (getter != null && getter.getDeclaringClass() != Object.class)
            {
                String getterName = getter.getName();

                try
                {
                    assertObjectEquals(getter.getName(), getter.invoke(a), getter.invoke(b));
                }
                catch (Exception e)
                {
                    throw new RuntimeException("Checking property '" + getterName + "': " + e.getMessage(), e);
                }
            }
        }
    }
}
