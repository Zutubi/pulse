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

package com.zutubi.pulse.core.marshal.types;

import com.zutubi.pulse.core.engine.api.Addable;
import com.zutubi.tove.annotations.Ordered;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;

@SymbolicName("fakePulseFile")
public class FakePulseFile extends AbstractConfiguration
{
    @Addable("recipe") @Ordered
    private Map<String, FakeRecipe> recipes = new LinkedHashMap<String, FakeRecipe>();

    public Map<String, FakeRecipe> getRecipes()
    {
        return recipes;
    }

    public void setRecipes(Map<String, FakeRecipe> recipes)
    {
        this.recipes = recipes;
    }

    public FakeRecipe getRecipe(String name)
    {
        return recipes.get(name);
    }
}
