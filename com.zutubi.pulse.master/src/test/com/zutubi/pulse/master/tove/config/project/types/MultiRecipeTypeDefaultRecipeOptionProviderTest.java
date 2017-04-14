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

package com.zutubi.pulse.master.tove.config.project.types;

import com.zutubi.pulse.core.engine.RecipeConfiguration;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.tove.ui.forms.FormContext;

import java.util.Arrays;

public class MultiRecipeTypeDefaultRecipeOptionProviderTest extends PulseTestCase
{
    private MultiRecipeTypeDefaultRecipeOptionProvider provider = new MultiRecipeTypeDefaultRecipeOptionProvider();

    public void testNullInstance()
    {
        assertEquals(0, provider.getOptions(null, new FormContext("")).size());
    }

    public void testNoRecipes()
    {
        assertEquals(0, provider.getOptions(null, new FormContext(new MultiRecipeTypeConfiguration())).size());
    }

    public void testSimple()
    {
        MultiRecipeTypeConfiguration type = new MultiRecipeTypeConfiguration();
        type.addRecipe(new RecipeConfiguration("default"));
        type.addRecipe(new RecipeConfiguration("absolutely"));
        type.addRecipe(new RecipeConfiguration("fabulous"));
        assertEquals(Arrays.asList("absolutely", "default", "fabulous"), provider.getOptions(null, new FormContext(type)));
    }
}
