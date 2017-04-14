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

package com.zutubi.tove.ui.model;

import java.util.List;

/**
 * Used to preview a move refactoring, i.e. moving a template collection item to a new parent.
 */
public class MoveModel
{
    private String scope;
    private String key;
    private String newParentKey;
    private List<String> pathsToDelete;

    public MoveModel()
    {
    }

    public MoveModel(MoveModel input, List<String> pathsToDelete)
    {
        this.scope = input.getScope();
        this.key = input.getKey();
        this.newParentKey = input.getNewParentKey();
        this.pathsToDelete = pathsToDelete;
    }

    public String getScope()
    {
        return scope;
    }

    public String getKey()
    {
        return key;
    }

    public String getNewParentKey()
    {
        return newParentKey;
    }

    public List<String> getPathsToDelete()
    {
        return pathsToDelete;
    }
}
