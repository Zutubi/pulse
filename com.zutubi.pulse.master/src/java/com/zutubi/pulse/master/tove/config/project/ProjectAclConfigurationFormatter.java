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

import com.google.common.base.Function;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.tove.config.group.GroupConfiguration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Sort;

import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

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
        allowedActions = newArrayList(transform(allowedActions, new Function<String, String>()
        {
            public String apply(String s)
            {
                return messages.format(s + ".label");
            }
        }));
        
        Collections.sort(allowedActions, new Sort.StringComparator());
        return allowedActions.toString();
    }
}
