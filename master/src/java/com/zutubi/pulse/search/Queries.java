/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.search;

import com.zutubi.pulse.model.BuildResult;

/**
 * <class-comment/>
 */
public class Queries extends QueryFactory
{
    public SearchQuery<BuildResult> getBuildResults()
    {
        SearchQuery<BuildResult> q = new SearchQuery<BuildResult>(BuildResult.class);
        q.setSessionFactory(sessionFactory);
        return q;
    }
}
