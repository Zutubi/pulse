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

package com.zutubi.pulse.master.webwork.dispatcher.mapper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A resolver that is based on a fixed mapping of child name to child resolver.
 * Used for paths where all children are known statically.
 */
public abstract class StaticMapActionResolver extends ActionResolverSupport
{
    private Map<String, ActionResolver> map = new HashMap<String, ActionResolver>();

    public StaticMapActionResolver(String action)
    {
        super(action);
    }

    protected void addMapping(String name, ActionResolver child)
    {
        map.put(name, child);
    }

    public List<String> listChildren()
    {
        return new LinkedList<String>(map.keySet());
    }

    public ActionResolver getChild(String name)
    {
        return map.get(name);
    }
}
