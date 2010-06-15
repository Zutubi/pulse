package com.zutubi.pulse.master.model.persistence.hibernate;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.util.bean.ObjectFactory;

/**
 * This object is a {@link HibernateSearchQuery} factory.
 *
 * @see HibernateSearchQuery
 */
@SuppressWarnings({"unchecked"})
public class HibernateSearchQueries
{
    private ObjectFactory objectFactory;

    public HibernateSearchQuery<BuildResult> getBuildResults()
    {
        return objectFactory.buildBean(HibernateSearchQuery.class, new Class[]{Class.class}, new Object[]{BuildResult.class});
    }

    /**
     * Create a query used for searching for the ids of the specified type of object.
     *
     * @param type of object being searched for.
     * @return a configured HibernateSearchQuery instance.
     */
    public HibernateSearchQuery<Long> getIds(Class type)
    {
        return objectFactory.buildBean(HibernateSearchQuery.class, new Class[]{Class.class}, new Object[]{type});
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
