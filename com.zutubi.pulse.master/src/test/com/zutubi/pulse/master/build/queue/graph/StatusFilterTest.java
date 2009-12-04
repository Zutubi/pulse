package com.zutubi.pulse.master.build.queue.graph;

import static com.zutubi.pulse.core.dependency.ivy.IvyStatus.*;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.util.TreeNode;

public class StatusFilterTest extends GraphFilterTestCase
{
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    public void testDownstreamStatusFiltering()
    {
        Project util = project("util");
        Project lib = project("lib");

        TreeNode<GraphData> root = node(util, node(lib, dependency(util, "latest.milestone")));

        assertFilters(1, STATUS_INTEGRATION, root);
        assertFilters(0, STATUS_MILESTONE, root);
        assertFilters(0, STATUS_RELEASE, root);
    }

    public void testDownstreamFixedRevision()
    {
        Project util = project("util");
        Project lib = project("lib");

        TreeNode<GraphData> root = node(util, node(lib, dependency(util, "FIXED")));

        assertFilters(0, STATUS_INTEGRATION, root);
/*  this case needs more thought. For now, just disable any filtering for this case.
        assertFilters(0, "FIXED", root);
*/
    }

    public void testUpstreamStatusFiltering()
    {
        Project util = project("util");
        Project lib = project("lib");

        TreeNode<GraphData> root = node(lib, node(util, dependency(util, "latest.milestone")));

        assertFilters(1, STATUS_INTEGRATION, root);
        assertFilters(0, STATUS_MILESTONE, root);
        assertFilters(0, STATUS_RELEASE, root);
    }

    public void testUpstreamFixedRevision()
    {
        Project util = project("util");
        Project lib = project("lib");

        TreeNode<GraphData> root = node(lib, node(util, dependency(util, "FIXED")));

        assertFilters(0, STATUS_INTEGRATION, root);
/*  this case needs more thought. For now, just disable any filtering for this case.
        assertFilters(0, "FIXED", root);
*/
    }

    private void assertFilters(int filterCount, String status, TreeNode<GraphData> node)
    {
        StatusFilter filter = new StatusFilter(status);
        applyFilter(filter, node);
        assertEquals(filterCount, filter.getToTrim().size());
    }
}
