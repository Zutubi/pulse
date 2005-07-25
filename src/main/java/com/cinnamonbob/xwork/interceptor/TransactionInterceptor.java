package com.cinnamonbob.xwork.interceptor;

import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.ActionProxy;
import com.opensymphony.xwork.interceptor.Interceptor;
import com.opensymphony.xwork.interceptor.PreResultListener;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An interceptor that wraps the action execution in a single Hibernate transaction.
 * 
 * NOTE: Code is adapted from com.atlassian.confluence.util.XWorkTransactionInterceptor. Will
 * need to receive clearance from Atlassian prior 'selling' this code.
 */
public class TransactionInterceptor implements Interceptor
{
    /**
     * Holder to support the currentTransactionStatus() method
     */
    private static ThreadLocal<TransactionStatus> currentTransactionStatus = new ThreadLocal<TransactionStatus>();

    private static final Logger LOG = Logger.getLogger(TransactionInterceptor.class.getName());

    /**
     * Delegate used to create, commit and rollback transactions
     */
    private PlatformTransactionManager transactionManager;

    /**
     * Set the transaction manager. This will perform actual
     * transaction management: This class is just a way of invoking it.
     */
    public void setTransactionManager(PlatformTransactionManager transactionManager)
    {
        this.transactionManager = transactionManager;
    }

    /**
     * Return the transaction status of the current method invocation.
     * Mainly intended for code that wants to set the current transaction
     * rollback-only but not throw an application exception.
     *
     */
    public static TransactionStatus currentTransactionStatus() throws RuntimeException
    {
        TransactionStatus status = currentTransactionStatus.get();
        if (status == null)
            throw new RuntimeException("No transaction status in scope");
        return status;
    }

    public void destroy()
    {
    }

    public void init()
    {
    }

    public PlatformTransactionManager getTransactionManager()
    {
        return transactionManager;
    }

    public String intercept(final ActionInvocation invocation) throws Exception
    {

        // If the transaction attribute is null, the method is non-transactional
        final TransactionAttribute transAtt = new DefaultTransactionAttribute(TransactionAttribute.PROPAGATION_REQUIRED);
        final TransactionStatus[] status = new TransactionStatus[1];
        TransactionStatus oldTransactionStatus;

        // We need a transaction for this method
        if (LOG.isLoggable(Level.INFO))
        {
            LOG.info("Getting transaction for action '" + getDetails(invocation.getProxy()) + "'");
        }

        // The transaction manager will flag an error if an incompatible tx already exists
        status[0] = getTransactionManager().getTransaction(transAtt);

        // Make the TransactionStatus available to callees
        oldTransactionStatus = currentTransactionStatus.get();
        currentTransactionStatus.set(status[0]);

        // Invoke the next interceptor in the chain.
        // This will normally result in a target object being invoked.
        String retVal = null;
        try
        {
            // We'll want to commit the transaction before the result is called.
            if (status[0] != null)
                invocation.addPreResultListener(new PreResultListener()
                {
                    public void beforeResult(ActionInvocation actionInvocation, String s)
                    {
                        if (LOG.isLoggable(Level.INFO))
                            LOG.info("Committing transaction for " + getDetails(invocation.getProxy()) + " before result");

                        getTransactionManager().commit(status[0]);

                        if (LOG.isLoggable(Level.INFO))
                            LOG.info("Opening new transaction for " + getDetails(invocation.getProxy()) + " result");

                        status[0] = getTransactionManager().getTransaction(transAtt);
                    }
                });

            retVal = invocation.invoke();
        }
        catch (Exception ex)
        {
            // Target invocation exception
            if (status[0] != null)
            {
                onThrowable(invocation, transAtt, status[0], ex);
            }
            throw ex;
        }
        catch (Throwable t)
        {
            // Target invocation exception
            if (status[0] != null)
            {
                onThrowable(invocation, transAtt, status[0], t);
            }
            throw new RuntimeException(t);
        }
        finally
        {
            // Use stack to restore old transaction status if one was set
            currentTransactionStatus.set(oldTransactionStatus);
        }
        if (status[0] != null)
        {
            if (LOG.isLoggable(Level.INFO))
            {
                LOG.info("Invoking commit for transaction on method '" + getDetails(invocation.getProxy()) + "'");
            }
            getTransactionManager().commit(status[0]);
        }
        return retVal;
    }

    private String getDetails(ActionProxy proxy)
    {
        String methodName = proxy.getConfig().getMethodName();

        if (methodName == null)
            methodName = "execute";

        String fullClazzName = proxy.getConfig().getClassName();
        String actionClazz = fullClazzName.substring(fullClazzName.lastIndexOf('.'));

        return proxy.getNamespace() + "/" + proxy.getActionName() + ".action (" + actionClazz + "." + methodName + "())";
    }

    /**
     * Handle a throwable.
     * We may commit or roll back, depending on our configuration.
     */
    private void onThrowable(ActionInvocation invocation, TransactionAttribute txAtt, TransactionStatus status, Throwable ex)
    {
        try
        {
            if (txAtt.rollbackOn(ex))
            {
                LOG.severe("Invoking rollback for transaction on action '" + getDetails(invocation.getProxy()) + "' due to throwable: " + ex);
                getTransactionManager().rollback(status);
            }
            else
            {
                if (LOG.isLoggable(Level.INFO))
                    LOG.info("Action '" + getDetails(invocation.getProxy()) + "' threw throwable but this does not force transaction rollback: " + ex);
                // Will still roll back if rollbackOnly is true
                getTransactionManager().commit(status);
            }
        }
        catch (Exception e)
        {
            // If an exception occurs during the rollback, there's no point propagating it -- it
            // would just hide the exception that was the root cause, making problem determination
            // much harder
            LOG.severe("Attempted rollback caused exception: " + e);
        }
    }
}