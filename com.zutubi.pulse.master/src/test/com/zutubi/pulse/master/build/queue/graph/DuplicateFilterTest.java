package com.zutubi.pulse.master.build.queue.graph;

import com.zutubi.pulse.master.model.Project;
import com.zutubi.util.TreeNode;

public class DuplicateFilterTest extends GraphFilterTestCase
{
    private DuplicateFilter filter;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        filter = new DuplicateFilter();
    }

    public void testNoUpstreamDuplicates()
    {
        Project util = project("util");
        Project lib = project("lib");
        Project client = project("client");

        TreeNode<BuildGraphData> root =
                node(client,
                        node(lib,
                                node(util)));

        applyFilter(filter, root);

        assertEquals(0, filter.getToTrim().size());
    }

    public void testNoDownstreamDuplicates()
    {
        Project util = project("util");
        Project lib = project("lib");
        Project client = project("client");

        TreeNode<BuildGraphData> root =
                node(util,
                        node(lib,
                                node(client)));

        applyFilter(filter, root);

        assertEquals(0, filter.getToTrim().size());
    }

    public void testUpstreamDuplicate()
    {
        Project util = project("util");
        Project libA = project("libA");
        Project libB = project("libB");
        Project client = project("client");

        TreeNode<BuildGraphData> root =
                node(client,
                        node(libA,
                                node(util)),
                        node(libB,
                                node(util)));

        applyFilter(filter, root);

        assertEquals(0, filter.getToTrim().size());

    }

    public void testUpstreamDuplicateWithChildren()
    {
        Project platform = project("platform");
        Project util = project("util");
        Project libA = project("libA");
        Project libB = project("libB");
        Project client = project("client");

        TreeNode<BuildGraphData> root =
                node(client,
                        node(libA,
                                node(util,
                                        node(platform))),
                        node(libB,
                                node(util, 
                                        node(platform))));

        applyFilter(filter, root);

        assertEquals(1, filter.getToTrim().size());
    }

    public void testDownstreamDuplicate()
    {
        Project platform = project("platform");
        Project utilA = project("utilA");
        Project utilB = project("utilB");
        Project lib = project("lib");

        TreeNode<BuildGraphData> root =
                node(platform,
                        node(utilA,
                                node(lib)),
                        node(utilB,
                                node(lib)));

        applyFilter(filter, root);

        assertEquals(0, filter.getToTrim().size());
    }

    public void testDownstreamDuplicateWithChildren()
    {
        Project platform = project("platform");
        Project utilA = project("utilA");
        Project utilB = project("utilB");
        Project lib = project("lib");
        Project client = project("client");

        TreeNode<BuildGraphData> root =
                node(platform,
                        node(utilA,
                                node(lib,
                                        node(client))),
                        node(utilB,
                                node(lib,
                                        node(client))));

        applyFilter(filter, root);

        assertEquals(1, filter.getToTrim().size());
    }
}
