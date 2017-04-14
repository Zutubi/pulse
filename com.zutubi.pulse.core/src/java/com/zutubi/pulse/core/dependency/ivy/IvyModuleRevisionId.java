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

package com.zutubi.pulse.core.dependency.ivy;

import org.apache.ivy.core.module.id.ModuleRevisionId;

import java.util.Map;

/**
 * A local version of the factory methods used by ivy to create module revision ids.
 *
 * @see ModuleRevisionId
 */
public class IvyModuleRevisionId
{
    private IvyModuleRevisionId()
    {
        // ensure that this class can not be instantiated.
    }

    public static ModuleRevisionId newInstance(String org, String module, String revision)
    {
        return ModuleRevisionId.newInstance(org, module, revision);
    }

    public static ModuleRevisionId newInstance(String org, String module, String revision, Map extraAttributes)
    {
        return ModuleRevisionId.newInstance(org, module, revision, extraAttributes);
    }

    public static ModuleRevisionId newInstance(String org, String name, String branch, String revision)
    {
        return ModuleRevisionId.newInstance(org, name, branch, revision);
    }
}
