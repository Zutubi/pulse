package com.zutubi.pulse.master.model;

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
    private List<List<BuildResult>> upstreamContexts = new LinkedList<List<BuildResult>>();

    /**
     * Creates a new upstream changelist for the given actual changelist found via the given build
     * path.
     * 
     * @param changelist the actual changelist
     * @param context    path in the upstream build graph at which the change was found
     */
    public UpstreamChangelist(PersistentChangelist changelist, List<BuildResult> context)
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
     * Indicates which upstream builds the change was found in.  Each item in the list is a single
     * chain of upstream builds, indicating the dependency path from the affected build (not
     * included in the list) to the build which directly included the change (the last item in the
     * list).  Multiple contexts are possible as the same changelist may be found via different
     * paths through the build dependency graph.  Shorter context paths appear earlier in this case.
     * 
     * @return a list of all build dependency graph paths from the affected build to the builds
     *         which this change directly affected
     */
    public List<List<BuildResult>> getUpstreamContexts()
    {
        return upstreamContexts;
    }

    void addUpstreamContext(List<BuildResult> context)
    {
        upstreamContexts.add(new LinkedList<BuildResult>(context));
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
}
