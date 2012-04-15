package com.zutubi.pulse.master.build.queue.graph;

import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.util.adt.TreeNode;

public class TransitiveFilterTest extends GraphFilterTestCase
{
    private TransitiveFilter filter;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        filter = new TransitiveFilter();
    }

    public void testNoneTransitiveDependenciesFilterChildren()
    {
        ProjectConfiguration client = project("client");
        ProjectConfiguration lib = project("lib");
        ProjectConfiguration util = project("util");

        TreeNode<BuildGraphData> node = node(client, node(lib, dependency(lib, false), node(util, dependency(util))));

        applyFilter(filter, node);
        assertEquals(1, filter.getToTrim().size());
    }

    public void testTransitiveDependenciesDoNotFilter()
    {
        ProjectConfiguration client = project("client");
        ProjectConfiguration lib = project("lib");
        ProjectConfiguration util = project("util");

        TreeNode<BuildGraphData> node = node(client, node(lib, dependency(lib), node(util, dependency(util))));

        applyFilter(filter, node);
        assertEquals(0, filter.getToTrim().size());
    }
}
