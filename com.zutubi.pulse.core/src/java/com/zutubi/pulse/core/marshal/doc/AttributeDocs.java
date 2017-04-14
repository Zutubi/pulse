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

package com.zutubi.pulse.core.marshal.doc;

/**
 * Stores documentation for a single attribute, including whether it is
 * required and any default value.
 */
public class AttributeDocs
{
    private String name;
    private String description;
    private boolean required;
    private String defaultValue;

    public AttributeDocs(String name, String description, boolean required, String defaultValue)
    {
        this.name = name;
        this.description = description;
        this.required = required;
        this.defaultValue = defaultValue;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public boolean isRequired()
    {
        return required;
    }

    public String getDefaultValue()
    {
        return defaultValue;
    }
}
