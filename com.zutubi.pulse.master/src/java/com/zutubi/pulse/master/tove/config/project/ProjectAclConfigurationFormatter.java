package com.zutubi.pulse.master.tove.config.project;

import com.google.common.base.Function;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.tove.config.group.GroupConfiguration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Sort;

import java.util.Collections;
import java.util.List;

/**
 * Formats fields of {@link ProjectAclConfiguration} instances for the UI.
 */
public class ProjectAclConfigurationFormatter
{
    public String getGroup(ProjectAclConfiguration configuration)
    {
        GroupConfiguration group = configuration.getGroup();
        return group == null ? null : group.getName();
    }

    public String getAllowedActions(ProjectAclConfiguration configuration)
    {
        List<String> allowedActions = configuration.getAllowedActions();
        final Messages messages = Messages.getInstance(ProjectAuthorityProvider.class);
        allowedActions = CollectionUtils.map(allowedActions, new Function<String, String>()
        {
            public String apply(String s)
            {
                return messages.format(s + ".label");
            }
        });
        
        Collections.sort(allowedActions, new Sort.StringComparator());
        return allowedActions.toString();
    }
}
