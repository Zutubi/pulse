package com.zutubi.pulse.core.model;

import com.zutubi.pulse.core.test.api.PulseTestCase;

import java.io.File;
import java.util.Map;

import static com.zutubi.util.CollectionUtils.asMap;
import static com.zutubi.util.CollectionUtils.asPair;

public class ResultCustomFieldsTest extends PulseTestCase
{
    private static final String PUNCTUATION = "`~!@#$%^&*()-_=+\\|]}[{'\";:/?.>,<";

    private File tmpDir;
    private ResultCustomFields resultCustomFields;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        tmpDir = createTempDirectory();
        resultCustomFields = new ResultCustomFields(tmpDir);
    }

    @Override
    protected void tearDown() throws Exception
    {
        removeDirectory(tmpDir);
        super.tearDown();
    }

    public void testSimpleProperties()
    {
        roundTrip(asMap(asPair("field1", "value1"), asPair("field2", "value2")));
    }

    public void testExoticPropertyName()
    {
        roundTrip(asMap(asPair(PUNCTUATION, "value")));
    }

    public void testExoticPropertyValue()
    {
        roundTrip(asMap(asPair("field", PUNCTUATION)));
    }

    public void testLoadAfterUpdate()
    {
        resultCustomFields.store(asMap(asPair("field1", "original value")));
        Map<String, String> fields = resultCustomFields.load();
        assertEquals(1, fields.size());
        assertEquals("original value", fields.get("field1"));

        resultCustomFields.store(asMap(asPair("field1", "new value"), asPair("field2", "value")));
        fields = resultCustomFields.load();
        assertEquals(2, fields.size());
        assertEquals("new value", fields.get("field1"));
        assertEquals("value", fields.get("field2"));
    }

    private void roundTrip(Map<String, String> fields)
    {
        resultCustomFields.store(fields);
        Map<String, String> loaded = resultCustomFields.load();
        assertNotSame(fields, loaded);
        assertEquals(fields, loaded);
    }
}
