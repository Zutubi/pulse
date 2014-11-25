package com.zutubi.pulse.master.xwork.interceptor;

import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.interceptor.Interceptor;
import com.zutubi.pulse.core.api.PulseRuntimeException;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate4.SessionFactoryUtils;
import org.springframework.orm.hibernate4.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.servlet.http.HttpServletRequest;

/**
 */
public class HibernateSessionInterceptor implements Interceptor
{
    private SessionFactory sessionFactory;

    public void destroy()
    {

    }

    public void init()
    {

    }

    public String intercept(ActionInvocation invocation) throws Exception
    {
        String result = null;

        before(invocation);
        try
        {
            result = invocation.invoke();
        }
        finally
        {
            after(invocation, result);
        }

        return result;
    }

    protected void before(ActionInvocation invocation) throws Exception
    {
        if (sessionFactory != null)
        {
            if (TransactionSynchronizationManager.hasResource(sessionFactory))
            {
                // Do not modify the Session: just mark the request accordingly.
                String reentryKey = getRentryKey();
                HttpServletRequest request = ServletActionContext.getRequest();
                Integer count = (Integer) request.getAttribute(reentryKey);
                int newCount = (count != null) ? count + 1 : 1;
                request.setAttribute(reentryKey, newCount);
            }
            else
            {
                Session session = openSession();
                TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
            }
        }
    }

    protected Session openSession()
    {
        try
        {
            Session session = sessionFactory.openSession();
            session.setFlushMode(FlushMode.MANUAL);
            return session;
        }
        catch (HibernateException ex)
        {
            throw new PulseRuntimeException("Could not open Hibernate Session", ex);
        }
    }

    protected void after(ActionInvocation dispatcher, String result) throws Exception
    {
        if (sessionFactory != null)
        {
            String reentryKey = getRentryKey();
            HttpServletRequest request = ServletActionContext.getRequest();

            Integer count = (Integer) request.getAttribute(reentryKey);
            if (count != null)
            {
                // Do not modify the Session: just clear the marker.
                if (count > 1)
                {
                    request.setAttribute(reentryKey, count - 1);
                }
                else
                {
                    request.removeAttribute(reentryKey);
                }
            }
            else
            {
                SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.unbindResource(sessionFactory);
                SessionFactoryUtils.closeSession(sessionHolder.getSession());
            }
        }
    }

    private String getRentryKey()
    {
        return sessionFactory.toString() + ".rentryCount";
    }

    public void setSessionFactory(SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }
}
