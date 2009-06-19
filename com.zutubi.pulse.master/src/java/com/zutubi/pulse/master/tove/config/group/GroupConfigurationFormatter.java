package com.zutubi.pulse.master.tove.config.group;

/**
 * Provide formatting for group configurations.  For built in groups, simply
 * the groups name is used.  For user groups, extra details such as the number
 * of members of the group is used.
 */
public class GroupConfigurationFormatter
{
    public String getMembers(GroupConfiguration group)
    {
        if(group instanceof BuiltinGroupConfiguration)
        {
            return group.getName();
        }
        else if(group instanceof UserGroupConfiguration)
        {
            int size = ((UserGroupConfiguration) group).getMembers().size();
            return String.format("%d user%s", size, size == 1 ? "" : "s");
        }
        return "unknown";
    }
}
