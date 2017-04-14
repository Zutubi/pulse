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

package com.zutubi.pulse.core.marshal.doc;

/**
 * Stores details of a child node as it nests within the context of some
 * parent element.  This includes the type-specific node docs as well as
 * the context-specific name and arity.
 */
public class ChildNodeDocs
{
    private String name;
    private NodeDocs nodeDocs;
    private Arity arity;

    public ChildNodeDocs(String name, NodeDocs nodeDocs, Arity arity)
    {
        this.name = name;
        this.nodeDocs = nodeDocs;
        this.arity = arity;
    }

    public String getName()
    {
        return name;
    }

    public NodeDocs getNodeDocs()
    {
        return nodeDocs;
    }

    public Arity getArity()
    {
        return arity;
    }
}
