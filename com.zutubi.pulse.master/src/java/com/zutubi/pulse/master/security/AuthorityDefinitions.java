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

package com.zutubi.pulse.master.security;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;

public class AuthorityDefinitions
{
    private Map<String, List<Privilege>> pathBasedPrivileges = new HashMap<String, List<Privilege>>();

    public void addPrivilege(String path, String role, String... methods)
    {
        getPrivileges(path).add(new Privilege(path, role, methods));
    }

    protected List<Privilege> getPrivileges(String path)
    {
        if (!pathBasedPrivileges.containsKey(path))
        {
            pathBasedPrivileges.put(path, new LinkedList<Privilege>());
        }
        return pathBasedPrivileges.get(path);
    }

    protected List<Privilege> getPrivileges()
    {
        List<Privilege> result = new LinkedList<Privilege>();
        for (List<Privilege> privileges : pathBasedPrivileges.values())
        {
            result.addAll(privileges);
        }
        return result;
    }
}
