package com.zutubi.pulse.master.xwork.actions.project;

import flexjson.JSON;

import java.util.LinkedList;
import java.util.List;

/**
 * Model for JSON data used to render the project history page.
 */
public class ProjectHistoryModel
{
    private List<BuildModel> builds = new LinkedList<BuildModel>();
    private PagerModel pager;

    public ProjectHistoryModel(List<BuildModel> builds, PagerModel pager)
    {
        this.builds = builds;
        this.pager = pager;
    }

    @JSON
    public List<BuildModel> getBuilds()
    {
        return builds;
    }

    public PagerModel getPager()
    {
        return pager;
    }
}
