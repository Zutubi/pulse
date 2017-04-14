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

package com.zutubi.tove.ui.model;

/**
 * Models a key-value property, with an option human-friendly label.  Supported value types are:
 * <ul>
 *     <li>String</li>
 *     <li>Collection&lt;String&gt;</li>
 *     <li>Collection&lt;KeyValueModel&gt;</li>
 * </ul>
 * In the latter case only one layer of nesting is allowed.
 */
public class KeyValueModel
{
    private String key;
    private String label;
    private Object value;

    public KeyValueModel(String key, Object value)
    {
        this.key = key;
        this.value = value;
    }

    public KeyValueModel(String key, String label, Object value)
    {
        this.key = key;
        this.label = label;
        this.value = value;
    }

    public String getKey()
    {
        return key;
    }

    public String getLabel()
    {
        return label;
    }

    public Object getValue()
    {
        return value;
    }
}
