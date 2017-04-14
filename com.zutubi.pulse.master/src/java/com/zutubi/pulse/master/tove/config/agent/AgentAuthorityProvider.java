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

package com.zutubi.pulse.master.tove.config.agent;

import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.ui.forms.FormContext;
import com.zutubi.tove.ui.forms.ListOptionProvider;
import com.zutubi.util.Sort;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Provides options for the available agent authorities, which includes
 * built in authorities like write as well as authorities mapped to
 * actions.
 */
public class AgentAuthorityProvider extends ListOptionProvider
{
    public String getEmptyOption(TypeProperty property, FormContext context)
    {
        return null;
    }

    public List<String> getOptions(TypeProperty property, FormContext context)
    {
        List<String> options = new LinkedList<String>();
        options.addAll(Arrays.asList(AccessManager.ACTION_ADMINISTER, AccessManager.ACTION_VIEW, AccessManager.ACTION_WRITE, AgentConfigurationActions.ACTION_DISABLE, AgentConfigurationActions.ACTION_PING));
        Collections.sort(options, new Sort.StringComparator());
        return options;
    }
}
