package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.engine.api.FieldScope;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorTestCase;
import com.zutubi.pulse.core.postprocessors.api.TestPostProcessorContext;
import com.zutubi.util.adt.Pair;

import java.io.IOException;
import java.util.Map;

import static com.zutubi.util.CollectionUtils.asPair;

public class CustomFieldsPostProcessorTest extends PostProcessorTestCase
{
    private static final String EXTENSION_PROPERTIES = "properties";

    public void testEmpty() throws IOException
    {
        TestPostProcessorContext context = runProcessor(createProcessor(), EXTENSION_PROPERTIES);
        assertEquals(0, context.getCustomFields().size());
    }

    public void testSimple() throws IOException
    {
        TestPostProcessorContext context = runProcessor(createProcessor(), EXTENSION_PROPERTIES);
        assertSimpleFields(context, FieldScope.RECIPE);
    }

    public void testBuildScope() throws IOException
    {
        CustomFieldsPostProcessorConfiguration config = new CustomFieldsPostProcessorConfiguration();
        config.setScope(FieldScope.BUILD);

        TestPostProcessorContext context = runProcessor(new CustomFieldsPostProcessor(config), "testSimple", EXTENSION_PROPERTIES);
        assertSimpleFields(context, FieldScope.BUILD);
    }

    private void assertSimpleFields(TestPostProcessorContext context, FieldScope scope)
    {
        Map<Pair<FieldScope,String>, String> fields = context.getCustomFields();
        assertEquals(2, fields.size());
        assertEquals("value1", fields.get(asPair(scope, "field1")));
        assertEquals("123", fields.get(asPair(scope, "field2")));
    }

    private CustomFieldsPostProcessor createProcessor()
    {
        return new CustomFieldsPostProcessor(new CustomFieldsPostProcessorConfiguration());
    }
}
