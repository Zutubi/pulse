package com.zutubi.pulse.web.admin.user;

import com.zutubi.pulse.model.Group;
import com.zutubi.pulse.model.NamedEntityComparator;

import java.util.List;
import java.util.Collections;

/**
 */
public class GroupActionSupport extends GroupsActionSupport
{
    private long groupId;
    protected Group group;

    public long getGroupId()
    {
        return groupId;
    }

    public void setGroupId(long groupId)
    {
        this.groupId = groupId;
    }

    public Group getGroup()
    {
        if(group == null)
        {
            group = getUserManager().getGroup(groupId);
        }

        return group;
    }
}
