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
import com.google.common.base.Predicate;
import static com.google.common.collect.Iterables.find;
import com.zutubi.tove.type.record.PathUtils;

import java.util.*;

/**
 * Implementation of nodes in the template hierarchy.  This should only be used
 * directly during refreshing of the hierarchy - all other access should be via
 * the {@link TemplateNode} interface.
 */
public class TemplateNodeImpl implements TemplateNode
{
    private TemplateNode parent;
    private List<TemplateNode> children = new ArrayList<>();
    private String path;
    private String id;
    private boolean concrete;

    public TemplateNodeImpl(String path, String id, boolean concrete)
    {
        this.concrete = concrete;
        this.path = path;
        this.id = id;
    }

    public TemplateNode getParent()
    {
        return parent;
    }

    private void setParent(TemplateNode parent)
    {
        this.parent = parent;
    }

    public List<TemplateNode> getChildren()
    {
        return Collections.unmodifiableList(children);
    }

    public TemplateNode getChild(final String id)
    {
        return find(children, new Predicate<TemplateNode>()
        {
            public boolean apply(TemplateNode templateNode)
            {
                return templateNode.getId().equals(id);
            }
        }, null);
    }

    public void addChild(TemplateNodeImpl child)
    {
        child.setParent(this);
        children.add(child);
    }

    public String getPath()
    {
        return path;
    }

    public String getId()
    {
        return id;
    }

    public boolean isConcrete()
    {
        return concrete;
    }

    public TemplateNode findNodeById(String id)
    {
        if(this.id.equals(id))
        {
            return this;
        }

        for(TemplateNode child: children)
        {
            TemplateNode node = child.findNodeById(id);
            if(node != null)
            {
                return node;
            }
        }

        return null;
    }

    public String getTemplatePath()
    {
        if(parent == null)
        {
            return id;
        }
        else
        {
            return PathUtils.getPath(parent.getTemplatePath(), id);
        }
    }

    public int getDepth()
    {
        if(parent == null)
        {
            return 0;
        }
        else
        {
            return parent.getDepth() + 1;
        }
    }

    public void forEachAncestor(Function<TemplateNode, Boolean> callback, boolean strict)
    {
        if((strict || Boolean.TRUE == callback.apply(this)) && parent != null)
        {
            parent.forEachAncestor(callback, false);
        }
    }

    public void forEachDescendant(Function<TemplateNode, Boolean> callback, boolean strict, Comparator<TemplateNode> comparator)
    {
        if (strict || Boolean.TRUE == callback.apply(this))
        {
            List<TemplateNode> children;
            if (comparator == null)
            {
                children = this.children;
            }
            else
            {
                children = new LinkedList<>(this.children);
                Collections.sort(children, comparator);
            }
            
            for (TemplateNode node: children)
            {
                node.forEachDescendant(callback, false, null);
            }
        }
    }
}
