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

import java.util.ArrayList;
import java.util.List;

/**
 * Models an item of a templated collection, with child nodes.
 */
public class TemplateNodeModel
{
    private String name;
    private long handle;
    private boolean concrete;
    private List<TemplateNodeModel> nested;

    public TemplateNodeModel(String name, long handle, boolean concrete)
    {
        this.name = name;
        this.handle = handle;
        this.concrete = concrete;
    }

    public String getName()
    {
        return name;
    }

    public long getHandle()
    {
        return handle;
    }

    public boolean isConcrete()
    {
        return concrete;
    }

    public List<TemplateNodeModel> getNested()
    {
        return nested;
    }

    public void addChild(TemplateNodeModel child)
    {
        if (nested == null)
        {
            nested = new ArrayList<>();
        }

        nested.add(child);
    }
}
