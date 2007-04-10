package com.zutubi.pulse.prototype.config;

import com.zutubi.prototype.annotation.TypeSelect;
import com.zutubi.validation.annotations.Required;

import java.util.List;

/**
 *
 *
 */
public class ScmConfiguration
{
    private List<String> filterPaths;

    public List<String> getFilterPaths()
    {
        return filterPaths;
    }

    public void setFilterPaths(List<String> filterPaths)
    {
        this.filterPaths = filterPaths;
    }    
}
