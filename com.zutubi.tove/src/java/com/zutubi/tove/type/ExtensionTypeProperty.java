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

import java.util.Map;

/**
 * A property that is not statically declared in code, but rather added at
 * run time.  This allows a type to be extended beyond the fields in the
 * original compiled code.
 */
public class ExtensionTypeProperty extends TypeProperty
{
    public ExtensionTypeProperty(String name, Type type)
    {
        super(name, type);
    }

    public Object getValue(Object instance) throws Exception
    {
        // get extensions map from instance.
        Map<String, Object> extensions = ((Extendable)instance).getExtensions();
        
        return extensions.get(getName());
    }

    public void setValue(Object instance, Object value)
    {
        // get extensions map from instance.
        Map<String, Object> extensions = ((Extendable)instance).getExtensions();
        extensions.put(getName(), value);
    }

    public boolean isReadable()
    {
        return true;
    }

    public boolean isWritable()
    {
        return true;
    }
}
