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

package com.zutubi.pulse.core.scm.cvs.client.commands;

/**
 *
 *
 */
public class RlsInfo
{
    private String module;
    private String name;
    private boolean directory;

    public RlsInfo(String module, String name, boolean directory)
    {
        this.module = module;
        this.name = name;
        this.directory = directory;
    }

    public String getModule()
    {
        return module;
    }

    public String getName()
    {
        return name;
    }

    public boolean isDirectory()
    {
        return directory;
    }

    public boolean isFile()
    {
        return !isDirectory();
    }


    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RlsInfo rlsInfo = (RlsInfo) o;

        if (directory != rlsInfo.directory) return false;
        if (module != null ? !module.equals(rlsInfo.module) : rlsInfo.module != null) return false;
        if (name != null ? !name.equals(rlsInfo.name) : rlsInfo.name != null) return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (module != null ? module.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (directory ? 1 : 0);
        return result;
    }
}
