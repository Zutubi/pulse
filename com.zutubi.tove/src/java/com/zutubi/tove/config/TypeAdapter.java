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

package com.zutubi.tove.config;

import com.zutubi.tove.config.api.Configuration;

/**
 */
public abstract class TypeAdapter<X extends Configuration> extends TypeListener<X>
{
    public TypeAdapter(Class<X> configurationClass)
    {
        super(configurationClass);
    }

    public void insert(X instance)
    {
        // noop
    }

    public void delete(X instance)
    {
        // noop
    }

    public void save(X instance, boolean nested)
    {
        // noop
    }

    public void postInsert(X instance)
    {
        // noop
    }

    public void postDelete(X instance)
    {
        // noop
    }


    public void postSave(X instance, boolean nested)
    {
        // noop
    }
}
