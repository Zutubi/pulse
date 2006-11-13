package com.zutubi.pulse.web.admin.user;

import com.zutubi.pulse.model.Group;
import com.zutubi.pulse.model.GrantedAuthority;

import java.util.Arrays;

/**
 */
public class CreateGroupAction extends GroupBasicsActionSupport
{
    private String name;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void validate()
    {
        if(hasErrors())
        {
            return;
        }

        if(getUserManager().getGroup(name) != null)
        {
            addFieldError("name", getText("group.name.duplicate", Arrays.asList(new Object[] { name })));
        }
    }

    public String execute() throws Exception
    {
        group = new Group(name);
        setPermissions(group);
        getUserManager().addGroup(group);
        setStartPage(getGroupStartPage(group));
        return SUCCESS;
    }
}
