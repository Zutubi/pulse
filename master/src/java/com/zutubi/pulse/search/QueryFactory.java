/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.search;

import org.hibernate.SessionFactory;

/**
 * <class-comment/>
 */
public class QueryFactory
{
    protected SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }
}
