/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.i18n.Messages;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.ui.forms.FormContext;
import com.zutubi.tove.ui.forms.MapOptionProvider;
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

    public Option getEmptyOption(TypeProperty property, FormContext context)
    {
        return null;
    }

    public Map<String, String> getMap(TypeProperty property, FormContext context)
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
