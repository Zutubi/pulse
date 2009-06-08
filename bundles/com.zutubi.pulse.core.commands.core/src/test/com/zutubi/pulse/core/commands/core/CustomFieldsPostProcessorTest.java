package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.postprocessors.api.PostProcessorTestCase;
import com.zutubi.pulse.core.postprocessors.api.TestPostProcessorContext;

import java.io.IOException;
import java.util.Map;

public class CustomFieldsPostProcessorTest extends PostProcessorTestCase
{
    private static final String EXTENSION_PROPERTIES = "properties";

    public void testEmpty() throws IOException
    {
        TestPostProcessorContext context = runProcessor(new CustomFieldsPostProcessor(), EXTENSION_PROPERTIES);
        assertEquals(0, context.getCustomFields().size());
    }

    public void testSimple() throws IOException
    {
        TestPostProcessorContext context = runProcessor(new CustomFieldsPostProcessor(), EXTENSION_PROPERTIES);
        Map<String,String> fields = context.getCustomFields();
        assertEquals(2, fields.size());
        assertEquals("value1", fields.get("field1"));
        assertEquals("123", fields.get("field2"));
    }
}
