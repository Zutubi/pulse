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

import com.zutubi.tove.type.CollectionType;
import com.zutubi.tove.type.ComplexType;
import com.zutubi.tove.type.Type;

/**
 * Holds information about a root configuration scope.
 */
public class ConfigurationScopeInfo
{
    private String scopeName;
    private ComplexType type;
    private boolean persistent;

    public ConfigurationScopeInfo(String scopeName, ComplexType type, boolean persistent)
    {
        this.scopeName = scopeName;
        this.type = type;
        this.persistent = persistent;
    }

    public String getScopeName()
    {
        return scopeName;
    }

    public ComplexType getType()
    {
        return type;
    }

    public boolean isPersistent()
    {
        return persistent;
    }

    public Type getTargetType()
    {
        return type.getTargetType();
    }

    public boolean isCollection()
    {
        return type instanceof CollectionType;
    }

    public boolean isTemplated()
    {
        return type.isTemplated();
    }
}
