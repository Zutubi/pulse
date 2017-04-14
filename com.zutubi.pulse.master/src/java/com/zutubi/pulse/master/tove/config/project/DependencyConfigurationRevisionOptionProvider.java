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

import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.ui.forms.FormContext;
import com.zutubi.tove.ui.forms.ListOptionProvider;

import java.util.Arrays;
import java.util.List;

import static com.zutubi.pulse.master.tove.config.project.DependencyConfiguration.*;

/**
 * The option provider implementation for the DependencyConfiguration's revision field.
 */
public class DependencyConfigurationRevisionOptionProvider extends ListOptionProvider
{
    public String getEmptyOption(TypeProperty property, FormContext context)
    {
        return null;
    }

    public List<String> getOptions(TypeProperty property, FormContext context)
    {
        return Arrays.asList(REVISION_LATEST_INTEGRATION, REVISION_LATEST_MILESTONE, REVISION_LATEST_RELEASE, REVISION_CUSTOM);
    }
}
