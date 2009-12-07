package com.zutubi.pulse.master.build.queue.graph;

import static com.zutubi.pulse.core.dependency.ivy.IvyStatus.*;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.util.TreeNode;

public class StatusFilterTest extends GraphFilterTestCase
{
    public void testDownstreamStatusFiltering()
    {
        Project util = project("util");
        Project lib = project("lib");

        TreeNode<BuildGraphData> root = node(util, node(lib, dependency(util, "latest.milestone")));

        assertFilters(1, STATUS_INTEGRATION, root);
        assertFilters(0, STATUS_MILESTONE, root);
        assertFilters(0, STATUS_RELEASE, root);
    }

    public void testDownstreamFixedRevision()
    {
        Project util = project("util");
        Project lib = project("lib");

        TreeNode<BuildGraphData> root = node(util, node(lib, dependency(util, "FIXED")));

        assertFilters(0, STATUS_INTEGRATION, root);
    }

    public void testUpstreamStatusFiltering()
    {
        Project util = project("util");
        Project lib = project("lib");

        TreeNode<BuildGraphData> root = node(lib, node(util, dependency(util, "latest.milestone")));

        assertFilters(1, STATUS_INTEGRATION, root);
        assertFilters(0, STATUS_MILESTONE, root);
        assertFilters(0, STATUS_RELEASE, root);
    }

    public void testUpstreamFixedRevision()
    {
        Project util = project("util");
        Project lib = project("lib");

        TreeNode<BuildGraphData> root = node(lib, node(util, dependency(util, "FIXED")));

        assertFilters(0, STATUS_INTEGRATION, root);
    }

    private void assertFilters(int filterCount, String status, TreeNode<BuildGraphData> node)
    {
        StatusFilter filter = new StatusFilter(status);
        applyFilter(filter, node);
        assertEquals(filterCount, filter.getToTrim().size());
    }
}
