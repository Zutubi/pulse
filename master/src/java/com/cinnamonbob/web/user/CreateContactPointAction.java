package com.cinnamonbob.web.user;

import java.util.HashMap;
import java.util.Map;

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
            types = new HashMap();
            types.put("email", "Email");
            types.put("yahoo", "Yahoo Messenger");
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
