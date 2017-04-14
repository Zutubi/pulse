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

package com.zutubi.tove.ui.forms;

import com.zutubi.tove.type.TypeProperty;

import java.util.List;

/**
 * Base for providers where the value and text are the same, so the options are just strings.
 */
public abstract class ListOptionProvider implements OptionProvider
{
    public abstract String getEmptyOption(TypeProperty property, FormContext context);
    public abstract List<String> getOptions(TypeProperty property, FormContext context);

    public String getOptionValue()
    {
        return null;
    }

    public String getOptionText()
    {
        return null;
    }
}
