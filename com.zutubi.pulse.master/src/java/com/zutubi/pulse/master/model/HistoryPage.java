package com.zutubi.pulse.master.model;

import java.util.List;

/**
 */
public class HistoryPage
{
    private Project project;
    private int totalBuilds;
    private int first;
    private int max;
    List<BuildResult> results;

    public HistoryPage(Project project, int first, int max)
    {
        this.project = project;
        this.first = first;
        this.max = max;
    }

    public Project getProject()
    {
        return project;
    }

    public int getTotalBuilds()
    {
        return totalBuilds;
    }

    public void setTotalBuilds(int totalBuilds)
    {
        this.totalBuilds = totalBuilds;
    }

    public int getFirst()
    {
        return first;
    }

    public void setFirst(int first)
    {
        this.first = first;
    }

    public int getMax()
    {
        return max;
    }

    public void setMax(int max)
    {
        this.max = max;
    }

    public List<BuildResult> getResults()
    {
        return results;
    }

    public void setResults(List<BuildResult> results)
    {
        this.results = results;
    }
}
