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

package com.zutubi.tove.type;

import com.zutubi.tove.type.record.RecordUtils;

/**
 * Simple types are stored as strings and editable as fields in a form.
 */
public abstract class SimpleType extends AbstractType
{
    public SimpleType(Class clazz)
    {
        super(clazz);
    }

    public SimpleType(Class type, String symbolicName)
    {
        super(type, symbolicName);
    }

    public boolean deepValueEquals(Object data1, Object data2)
    {
        return RecordUtils.valuesEqual(data1, data2);
    }

    public void initialise(Object instance, Object data, Instantiator instantiator)
    {
        // Nothing to to
    }

    public abstract String fromXmlRpc(String templateOwnerPath, Object data, boolean applyDefaults) throws TypeException;

    public String toString()
    {
        return getClazz().getSimpleName().toLowerCase();
    }
}
