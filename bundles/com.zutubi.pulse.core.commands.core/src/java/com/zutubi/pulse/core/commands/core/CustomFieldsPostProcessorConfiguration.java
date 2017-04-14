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

package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.engine.api.FieldScope;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfigurationSupport;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * Configuration for instances of {@link CustomFieldsPostProcessor}.
 */
@SymbolicName("zutubi.customFieldPostProcessorConfig")
public class CustomFieldsPostProcessorConfiguration extends PostProcessorConfigurationSupport
{
    private FieldScope scope = FieldScope.RECIPE;

    public CustomFieldsPostProcessorConfiguration()
    {
        super(CustomFieldsPostProcessor.class);
    }

    public CustomFieldsPostProcessorConfiguration(String name)
    {
        super(CustomFieldsPostProcessor.class);
        setName(name);
    }

    public FieldScope getScope()
    {
        return scope;
    }

    public void setScope(FieldScope scope)
    {
        this.scope = scope;
    }
}

