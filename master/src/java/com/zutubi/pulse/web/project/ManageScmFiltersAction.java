package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.Scm;

import java.util.List;

/**
 * 
 */
public class ManageScmFiltersAction extends ProjectActionSupport
{
    private String excludedPath = null;

    private long id;

    private int index;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public int getIndex()
    {
        return index;
    }

    public void setIndex(int index)
    {
        this.index = index;
    }

    public String getExcludedPath()
    {
        return excludedPath;
    }

    public void setExcludedPath(String excludedPath)
    {
        this.excludedPath = excludedPath;
    }

    public String doDelete() throws Exception
    {
        Scm scm = getScmManager().getScm(id);
        List<String> paths = scm.getFilteredPaths();
        paths.remove(index);
        scm.setFilteredPaths(paths);
        getScmManager().save(scm);

        return SUCCESS;
    }

    public String doAdd() throws Exception
    {
        Scm scm = getScmManager().getScm(id);
        scm.addExcludedPath(excludedPath);
        getScmManager().save(scm);

        // clear the excludedPath so that we get a fresh form.
        excludedPath = null;

        return SUCCESS;
    }

    public String execute() throws Exception
    {
        return SUCCESS;
    }
}
