package com.zutubi.pulse.master.xwork.interceptor;

import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.interceptor.Interceptor;
import com.zutubi.util.logging.Logger;
import org.hibernate.FlushMode;
import org.hibernate.SessionFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * An interceptor to wrap a read-only transaction around a request.  Used to
 * ensure that nothing will be flushed at the end of the hibernate session.
 */
public class ReadOnlyInterceptor implements Interceptor
{
    private static final Logger LOG = Logger.getLogger(ReadOnlyInterceptor.class);

    private PlatformTransactionManager transactionManager;
    private SessionFactory sessionFactory;

    public void init()
    {
    }

    public void destroy()
    {
    }

    public String intercept(ActionInvocation invocation) throws Exception
    {
        sessionFactory.getCurrentSession().setFlushMode(FlushMode.NEVER);

        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setReadOnly(true);
        TransactionStatus status = transactionManager.getTransaction(definition);
        String result;
        try
        {
            result = invocation.invoke();
        }
        catch (Exception e)
        {
            // Transactional code threw application exception -> rollback
            rollbackOnException(status, e);
            throw e;
        }
        catch (Error err)
        {
            // Transactional code threw error -> rollback
            rollbackOnException(status, err);
            throw err;
        }

        this.transactionManager.commit(status);
        return result;

    }

    private void rollbackOnException(TransactionStatus status, Throwable ex) throws TransactionException
    {
        try
        {
            this.transactionManager.rollback(status);
        }
        catch (RuntimeException ex2)
        {
            LOG.error("Application exception overridden by rollback exception", ex);
            throw ex2;
        }
        catch (Error err)
        {
            LOG.error("Application exception overridden by rollback error", ex);
            throw err;
        }
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager)
    {
        this.transactionManager = transactionManager;
    }

    public void setSessionFactory(SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }
}
