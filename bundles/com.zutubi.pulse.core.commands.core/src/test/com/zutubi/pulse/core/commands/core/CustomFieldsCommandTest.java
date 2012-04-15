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
