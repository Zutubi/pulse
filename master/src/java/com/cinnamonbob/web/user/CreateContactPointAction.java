package com.cinnamonbob.web.user;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 *
 */
public class CreateContactPointAction extends UserActionSupport
{
    private Map types;

    private String type;

    private long user;

    public Map getTypes()
    {
        if (types == null)
        {
            types = new TreeMap();
            types.put("email", "email");
            types.put("yahoo", "yahoo messenger");
        }
        return types;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getType()
    {
        return this.type;
    }

    public long getUser()
    {
        return this.user;
    }

    public void setUser(long user)
    {
        this.user = user;
    }
}
