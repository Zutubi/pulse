package com.zutubi.pulse.master.build.queue.graph;

import com.zutubi.pulse.master.model.Project;
import com.zutubi.util.TreeNode;

public class TransitiveFilterTest extends GraphFilterTestCase
{
    private TransitiveFilter filter;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        filter = new TransitiveFilter();
    }

    public void testTransitiveUpstream()
    {
        Project client = project("client");
        Project lib = project("lib");
        Project util = project("util");

        TreeNode<GraphData> node = node(client, node(lib, dependency(lib, false), node(util, dependency(util))));

        applyFilter(filter, node);
        assertEquals(1, filter.getToTrim().size());
    }
}
