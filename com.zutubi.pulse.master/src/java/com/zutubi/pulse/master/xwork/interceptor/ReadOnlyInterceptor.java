package com.zutubi.pulse.master.xwork.interceptor;

import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.interceptor.Interceptor;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 * An interceptor to wrap a read-only transaction around a request.  Used to
 * ensure that nothing will be flushed at the end of the hibernate session.
 */
public class ReadOnlyInterceptor implements Interceptor
{
    private SessionFactory sessionFactory;

    public void init()
    {
    }

    public void destroy()
    {
    }

    public String intercept(ActionInvocation invocation) throws Exception
    {
        Session session = sessionFactory.getCurrentSession();
        session.setFlushMode(FlushMode.MANUAL);
        return invocation.invoke();
    }

    public void setSessionFactory(SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }
}
