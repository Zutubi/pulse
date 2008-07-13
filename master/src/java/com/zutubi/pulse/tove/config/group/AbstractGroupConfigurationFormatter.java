package com.zutubi.pulse.tove.config.group;

import com.zutubi.pulse.model.GrantedAuthority;

/**
 */
public class AbstractGroupConfigurationFormatter
{
    public String getMembers(AbstractGroupConfiguration group)
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
        else if(group instanceof GroupConfiguration)
        {
            int size = ((GroupConfiguration) group).getMembers().size();
            return String.format("%d user%s", size, size == 1 ? "" : "s");
        }

        return "unknown";
    }
}
