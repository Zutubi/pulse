package com.zutubi.pulse.master.xwork.actions.project;

import flexjson.JSON;

import java.util.LinkedList;
import java.util.List;

/**
 * Model for JSON data used to render the project history page.
 */
public class ProjectHistoryModel
{
    private String url;
    private String stateFilter;
    private List<BuildModel> builds = new LinkedList<BuildModel>();
    private PagerModel pager;

    public ProjectHistoryModel(String url, String stateFilter, List<BuildModel> builds, PagerModel pager)
    {
        this.url = url;
        this.stateFilter = stateFilter;
        this.builds = builds;
        this.pager = pager;
    }

    public String getUrl()
    {
        return url;
    }

    public String getStateFilter()
    {
        return stateFilter;
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
