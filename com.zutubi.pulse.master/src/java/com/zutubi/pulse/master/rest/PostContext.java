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

package com.zutubi.pulse.master.rest;

import com.zutubi.tove.type.CompositeType;

/**
 * Captures contextual information about a configuration path for the purpose of POST'ing. A POST
 * should be used to insert new items into a collection path, or set a property of composite type
 * that is currently null.
 */
public class PostContext
{
    private String parentPath;
    private String baseName;
    private CompositeType postableType;

    public PostContext(String parentPath, String baseName, CompositeType postableType)
    {
        this.parentPath = parentPath;
        this.baseName = baseName;
        this.postableType = postableType;
    }

    public String getParentPath()
    {
        return parentPath;
    }

    public String getBaseName()
    {
        return baseName;
    }

    public CompositeType getPostableType()
    {
        return postableType;
    }
}
