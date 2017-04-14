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

package com.zutubi.pulse.master.tove.config.group;

import com.zutubi.tove.annotations.*;

/**
 */
@Internal
@SymbolicName("zutubi.builtinGroupConfig")
@Classification(single = "group")
@Form(fieldOrder = {"name", "serverPermissions", "concurrentPersonalBuilds"})
public class BuiltinGroupConfiguration extends GroupConfiguration
{
    @Internal
    private String role;

    public BuiltinGroupConfiguration()
    {
    }

    public BuiltinGroupConfiguration(String name, String role)
    {
        super(name);
        this.role = role;
    }

    @ReadOnly // we do not want people accidentally renaming the built in group names.
    public String getName()
    {
        return super.getName();
    }

    public String getRole()
    {
        return role;
    }

    public void setRole(String role)
    {
        this.role = role;
    }

    @Transient
    public String getDefaultAuthority()
    {
        return role;
    }
}
