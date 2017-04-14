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

package com.zutubi.pulse.master.model;

import com.google.common.base.Function;
import com.zutubi.pulse.core.model.PersistentChangelist;

import java.util.LinkedList;
import java.util.List;

/**
 * Information about a changelist that indirectly affected a build (via one or more upstream
 * dependencies).
 */
public class UpstreamChangelist
{
    private PersistentChangelist changelist;
    private List<BuildPath> upstreamContexts = new LinkedList<BuildPath>();

    /**
     * Creates a new upstream changelist for the given actual changelist found via the given build
     * path.
     * 
     * @param changelist the actual changelist
     * @param context    path in the upstream build graph at which the change was found
     */
    public UpstreamChangelist(PersistentChangelist changelist, BuildPath context)
    {
        this.changelist = changelist;
        upstreamContexts.add(context);
    }

    /**
     * @return the actual changelist
     */
    public PersistentChangelist getChangelist()
    {
        return changelist;
    }

    /**
     * Indicates which upstream builds the change was found in.  Each item in the list is a path of
     * upstream builds, indicating the dependency path from the affected build (not included in the
     * path) to the build which directly included the change (the last item in the path).  Multiple
     * contexts are possible as the same changelist may be found via different paths through the
     * build dependency graph.  Shorter context paths appear earlier in this case.
     * 
     * @return a list of all build dependency graph paths from the affected build to the builds
     *         which this change directly affected
     */
    public List<BuildPath> getUpstreamContexts()
    {
        return upstreamContexts;
    }

    void addUpstreamContext(BuildPath context)
    {
        upstreamContexts.add(context);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        UpstreamChangelist that = (UpstreamChangelist) o;

        if (changelist != null ? !changelist.equals(that.changelist) : that.changelist != null)
        {
            return false;
        }
        if (upstreamContexts != null ? !upstreamContexts.equals(that.upstreamContexts) : that.upstreamContexts != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = changelist != null ? changelist.hashCode() : 0;
        result = 31 * result + (upstreamContexts != null ? upstreamContexts.hashCode() : 0);
        return result;
    }

    /**
     * Maps from an upstream changelist to the actual changelist.
     */
    public static class ToChangelistFunction implements Function<UpstreamChangelist, PersistentChangelist>
    {
        public PersistentChangelist apply(UpstreamChangelist upstreamChangelist)
        {
            return upstreamChangelist.changelist;
        }
    }
}
