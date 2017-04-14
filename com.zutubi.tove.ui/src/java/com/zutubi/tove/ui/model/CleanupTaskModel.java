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
 * Models a task required to delete a config instance (and clean up after it).
 */
public class CleanupTaskModel
{
    private String path;
    private String summary;
    private List<CleanupTaskModel> children;

    public CleanupTaskModel(String path, String summary)
    {
        this.path = path;
        this.summary = summary;
    }

    public String getPath()
    {
        return path;
    }

    public String getSummary()
    {
        return summary;
    }

    public List<CleanupTaskModel> getChildren()
    {
        return children;
    }

    public void addChild(CleanupTaskModel child)
    {
        if (children == null)
        {
            children = new ArrayList<>();
        }

        children.add(child);
    }
}
