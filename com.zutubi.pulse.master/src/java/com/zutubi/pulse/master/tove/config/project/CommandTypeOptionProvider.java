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

import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.tove.ui.forms.ExtensionOptionProvider;
import com.zutubi.tove.ui.forms.FormContext;
import com.zutubi.tove.ui.forms.MapOptionProvider;

import java.util.List;
import java.util.Map;

/**
 * An option provider that lists all available command types, mapping to the
 * symbolic names.
 */
public class CommandTypeOptionProvider extends MapOptionProvider
{
    private TypeRegistry typeRegistry;

    public MapOptionProvider.Option getEmptyOption(TypeProperty property, FormContext context)
    {
        return null;
    }

    protected Map<String, String> getMap(TypeProperty property, FormContext context)
    {
        List<CompositeType> extensions = typeRegistry.getType(CommandConfiguration.class).getExtensions();
        ExtensionOptionProvider delegate = new ExtensionOptionProvider(extensions);
        return delegate.getMap(property, context);
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}
