package com.zutubi.pulse.search;

import com.zutubi.pulse.master.model.BuildResult;
import org.hibernate.SessionFactory;

/**
 * The Queries object is a SearchQuery factory that provides a way to create search queries.
 */
public class Queries
{
    protected SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }

    public SearchQuery<BuildResult> getBuildResults()
    {
        SearchQuery<BuildResult> q = new SearchQuery<BuildResult>(BuildResult.class);
        q.setSessionFactory(sessionFactory);
        return q;
    }

    /**
     * Create a query used for searching for the ids of the specified type of object.
     *
     * @param type of object being searched for.
     *
     * @return a configured SearchQuery instance
     */
    public SearchQuery<Long> getIds(Class type)
    {
        SearchQuery<Long> q = new SearchQuery<Long>(type);
        q.setSessionFactory(sessionFactory);
        return q;
    }

    public SearchQuery<String> getStrings(Class type)
    {
        SearchQuery<String> q = new SearchQuery<String>(type);
        q.setSessionFactory(sessionFactory);
        return q;
    }
}
