package com.cinnamonbob.web.project;

import com.cinnamonbob.model.Scm;

public class ScmActionSupport extends ProjectActionSupport
{
    private String name;
    private String path;
    
    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public String getPath()
    {
        return path;
    }
    public void setPath(String path)
    {
        this.path = path;
    }
    
    protected void setCommonFields(Scm scm)
    {
        scm.setName(name);
        scm.setPath(path);
    }
}
