package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.tove.handler.MapOption;
import com.zutubi.pulse.master.tove.handler.MapOptionProvider;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.util.Sort;

import java.util.*;

/**
 * Provides options for the available project authorities, which includes
 * built in authorities like view and write as well as authorities mapped to
 * actions.
 */
public class ProjectAuthorityProvider extends MapOptionProvider
{
    private static final Messages I18N = Messages.getInstance(ProjectAuthorityProvider.class);

    public MapOption getEmptyOption(Object instance, String parentPath, TypeProperty property)
    {
        return null;
    }

    public Map<String, String> getMap(Object instance, String parentPath, TypeProperty property)
    {
        Map<String, String> options = new HashMap<String, String>();
        addFormattedOptions(options, AccessManager.ACTION_ADMINISTER,
                                     AccessManager.ACTION_VIEW,
                                     AccessManager.ACTION_WRITE,
                                     ProjectConfigurationActions.ACTION_MARK_CLEAN,
                                     ProjectConfigurationActions.ACTION_PAUSE,
                                     ProjectConfigurationActions.ACTION_TRIGGER,
                                     ProjectConfigurationActions.ACTION_TRIGGER_HOOK,
                                     ProjectConfigurationActions.ACTION_CANCEL_BUILD,
                                     ProjectConfigurationActions.ACTION_VIEW_SOURCE);
        return options;
    }

    private void addFormattedOptions(Map<String, String> options, String... values)
    {
        List<String> sortedValues = Arrays.asList(values);
        Collections.sort(sortedValues, new Sort.StringComparator());
        for (String value: sortedValues)
        {
            options.put(value, I18N.format(value + ".label"));
        }
    }
}
