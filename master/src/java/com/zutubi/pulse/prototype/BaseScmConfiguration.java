package com.zutubi.pulse.prototype;

import com.zutubi.prototype.form.annotation.Table;

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
