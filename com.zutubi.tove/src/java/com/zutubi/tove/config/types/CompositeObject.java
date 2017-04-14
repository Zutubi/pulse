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

import com.zutubi.tove.annotations.ID;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;

import java.util.List;
import java.util.Map;

/**
 */
@SymbolicName("Composite")
public class CompositeObject extends AbstractConfiguration
{
    @ID
    private String strA;
    private String strB;
    private SimpleObject simple;

    private List<String> list;
    private Map<String, SimpleObject> map;

    public String getStrA()
    {
        return strA;
    }

    public void setStrA(String strA)
    {
        this.strA = strA;
    }

    public String getStrB()
    {
        return strB;
    }

    public void setStrB(String strB)
    {
        this.strB = strB;
    }

    public SimpleObject getSimple()
    {
        return simple;
    }

    public void setSimple(SimpleObject simple)
    {
        this.simple = simple;
    }

    public List<String> getList()
    {
        return list;
    }

    public void setList(List<String> list)
    {
        this.list = list;
    }

    public Map<String, SimpleObject> getMap()
    {
        return map;
    }

    public void setMap(Map<String, SimpleObject> map)
    {
        this.map = map;
    }
}
