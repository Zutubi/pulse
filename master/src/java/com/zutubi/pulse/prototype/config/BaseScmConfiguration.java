package com.zutubi.pulse.prototype.config;

import com.zutubi.prototype.annotation.Table;

import java.util.List;

/**
 *
 *
 */
public class BaseScmConfiguration
{
    private List<String> filterPaths;

    @Table()
    public List<String> getFilterPaths()
    {
        return filterPaths;
    }

    public void setFilterPaths(List<String> filterPaths)
    {
        this.filterPaths = filterPaths;
    }
}
