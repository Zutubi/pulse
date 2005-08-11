package com.cinnamonbob.web.project;

import java.util.TreeMap;
import java.util.Map;

/**
 * 
 *
 */
public class CreateScmAction extends ProjectActionSupport
{
    private long id;
    private String type;

    private Map<String, String> types;

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public Map getTypes()
    {
        if (types == null)
        {
            types = new TreeMap<String, String>();
            types.put("svn", "Subversion");
            types.put("p4", "Perforce");
            types.put("cvs", "CVS");
        }
        return types;
    }

    public String doDefault()
    {
        return SUCCESS;
    }

    public String execute()
    {
        return SUCCESS;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }
}
