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


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A simple holder for the path, role, methods tuple.  The represented data is defined as follows:
 * <ul>
 * <li>Path</li> is the http request path for which this privilege is being applied.
 * <li>Role</li> is the role required to qualify for this privilege.
 * <li>Methods</li> are the methods that are 'allowed' by this privilege. 
 * </ul>
 */
public class Privilege
{
    private String path;
    private String role;
    private Set<String> methods;

    protected Privilege(String path, String role, String... methods)
    {
        this.path = path;
        this.role = role;
        this.methods = new HashSet<String>();
        this.methods.addAll(Arrays.asList(methods));
    }

    public String getPath()
    {
        return path;
    }

    public String getRole()
    {
        return role;
    }

    public Set<String> getMethods()
    {
        return methods;
    }

    public boolean containsMethod(final String method)
    {
        return methods.contains(method);
    }
}
