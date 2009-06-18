package com.zutubi.pulse.master.tove.config.group;

import com.zutubi.pulse.master.model.GrantedAuthority;

/**
 */
public class GroupConfigurationFormatter
{
    public String getMembers(GroupConfiguration group)
    {
        if(group instanceof BuiltinGroupConfiguration)
        {
            String role = group.getDefaultAuthority();
            if(role.equals(GrantedAuthority.GUEST))
            {
                return "anonymous users";
            }
            else
            {
                return "all users";
            }
        }
        else if(group instanceof UserGroupConfiguration)
        {
            int size = ((UserGroupConfiguration) group).getMembers().size();
            return String.format("%d user%s", size, size == 1 ? "" : "s");
        }

        return "unknown";
    }
}
