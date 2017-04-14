/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        return objectFactory.buildBean(HibernateSearchQuery.class, BuildResult.class);
    }

    /**
     * Create a query used for searching for the ids of the specified type of object.
     *
     * @param type of object being searched for.
     * @return a configured HibernateSearchQuery instance.
     */
    public HibernateSearchQuery<Long> getIds(Class type)
    {
        return objectFactory.buildBean(HibernateSearchQuery.class, type);
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
