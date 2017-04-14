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

package com.zutubi.tove.config;

import com.google.common.base.Function;
import com.zutubi.tove.type.record.PathUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * A hierarchy of template configuration meta-data, used to display the
 * template tree and retrieve further information if required.
 * <p/>
 * This class is designed to be immutable (as indeed are the nodes).
 */
public class TemplateHierarchy
{
    private String scope;
    private TemplateNode root;
    /**
     * Looking up nodes by id happens frequently, so to make it fast we cache a mapping.
     */
    private Map<String, TemplateNode> nodesById = new HashMap<String, TemplateNode>();

    public TemplateHierarchy(String scope, TemplateNode root)
    {
        this.scope = scope;
        this.root = root;
        if (root != null)
        {
            root.forEachDescendant(new Function<TemplateNode, Boolean>()
            {
                public Boolean apply(TemplateNode input)
                {
                    nodesById.put(input.getId(), input);
                    return true;
                }
            }, false, null);
        }
    }

    public String getScope()
    {
        return scope;
    }

    public TemplateNode getRoot()
    {
        return root;
    }

    public TemplateNode getNodeByTemplatePath(String path)
    {
        TemplateNode current = null;

        String[] elements = PathUtils.getPathElements(path);
        if(elements.length > 0 && elements[0].equals(root.getId()))
        {
            current = root;
            for(int i = 1; current != null && i < elements.length; i++)
            {
                current = current.getChild(elements[i]);
            }
        }

        return current;
    }

    public TemplateNode getNodeById(String id)
    {
        return nodesById.get(id);
    }
}
