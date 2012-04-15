package com.zutubi.pulse.master.build.queue.graph;

import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.util.adt.TreeNode;

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
        ProjectConfiguration util = project("util");
        ProjectConfiguration lib = project("lib");
        ProjectConfiguration client = project("client");

        TreeNode<BuildGraphData> root =
                node(client,
                        node(lib,
                                node(util)));

        applyFilter(filter, root);

        assertEquals(0, filter.getToTrim().size());
    }

    public void testNoDownstreamDuplicates()
    {
        ProjectConfiguration util = project("util");
        ProjectConfiguration lib = project("lib");
        ProjectConfiguration client = project("client");

        TreeNode<BuildGraphData> root =
                node(util,
                        node(lib,
                                node(client)));

        applyFilter(filter, root);

        assertEquals(0, filter.getToTrim().size());
    }

    public void testUpstreamDuplicate()
    {
        ProjectConfiguration util = project("util");
        ProjectConfiguration libA = project("libA");
        ProjectConfiguration libB = project("libB");
        ProjectConfiguration client = project("client");

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
        ProjectConfiguration platform = project("platform");
        ProjectConfiguration util = project("util");
        ProjectConfiguration libA = project("libA");
        ProjectConfiguration libB = project("libB");
        ProjectConfiguration client = project("client");

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
        ProjectConfiguration platform = project("platform");
        ProjectConfiguration utilA = project("utilA");
        ProjectConfiguration utilB = project("utilB");
        ProjectConfiguration lib = project("lib");

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
        ProjectConfiguration platform = project("platform");
        ProjectConfiguration utilA = project("utilA");
        ProjectConfiguration utilB = project("utilB");
        ProjectConfiguration lib = project("lib");
        ProjectConfiguration client = project("client");

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
