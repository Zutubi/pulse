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

package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.commands.api.CommandTestCase;
import com.zutubi.pulse.core.commands.api.TestCommandContext;
import com.zutubi.pulse.core.engine.api.FieldScope;
import com.zutubi.util.adt.Pair;

import java.util.Map;

import static com.zutubi.util.CollectionUtils.asPair;

public class CustomFieldsCommandTest extends CommandTestCase
{
    public void testSimple() throws Exception
    {
        CustomFieldsCommandConfiguration config = new CustomFieldsCommandConfiguration("fields");
        config.addField(new CustomFieldConfiguration("build.prop", "build.val", FieldScope.BUILD));
        config.addField(new CustomFieldConfiguration("recipe.prop", "recipe.val", FieldScope.RECIPE));

        TestCommandContext context = runCommand(new CustomFieldsCommand(config));
        
        Map<Pair<FieldScope, String>,String> fields = context.getCustomFields();
        assertEquals("build.val", fields.get(asPair(FieldScope.BUILD, "build.prop")));
        assertEquals("recipe.val", fields.get(asPair(FieldScope.RECIPE, "recipe.prop")));
    }
}
