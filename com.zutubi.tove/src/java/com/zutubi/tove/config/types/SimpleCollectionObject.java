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

package com.zutubi.tove.config.types;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;

import java.util.List;

/**
 */
@SymbolicName("SimpleCollection")
public class SimpleCollectionObject extends AbstractConfiguration
{
    List<SimpleObject> simpleList;

    public List<SimpleObject> getSimpleList()
    {
        return simpleList;
    }

    public void setSimpleList(List<SimpleObject> simpleList)
    {
        this.simpleList = simpleList;
    }
}
