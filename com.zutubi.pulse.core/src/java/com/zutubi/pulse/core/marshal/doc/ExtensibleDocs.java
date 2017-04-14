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

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Represents an extensible type in the tree.  All extensions are grouped under
 * this node, making for a logical grouping.
 */
public class ExtensibleDocs extends NodeDocs
{
    private SortedMap<String, ElementDocs> extensions = new TreeMap<String, ElementDocs>();

    public ExtensibleDocs(String brief, String verbose)
    {
        super(brief, verbose);
    }

    public boolean hasExtension(String name)
    {
        return extensions.containsKey(name);
    }

    public ElementDocs getExtension(String name)
    {
        return extensions.get(name);
    }

    public Map<String, ElementDocs> getExtensions()
    {
        return extensions;
    }

    public void addExtension(String name, ElementDocs elementDocs)
    {
        this.extensions.put(name, elementDocs);
    }

    public void removeExtension(String name)
    {
        extensions.remove(name);
    }

    @Override
    public NodeDocs getNode(String name)
    {
        return getExtension(name);
    }

    @Override
    public boolean isElement()
    {
        return false;
    }
}
