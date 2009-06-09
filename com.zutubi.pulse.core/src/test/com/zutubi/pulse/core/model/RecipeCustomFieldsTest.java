package com.zutubi.pulse.core.model;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import static com.zutubi.util.CollectionUtils.asMap;
import static com.zutubi.util.CollectionUtils.asPair;
import com.zutubi.util.FileSystemUtils;

import java.io.File;
import java.util.Map;

public class RecipeCustomFieldsTest extends PulseTestCase
{
    private static final String PUNCTUATION = "`~!@#$%^&*()-_=+\\|]}[{'\";:/?.>,<";

    private File tmpDir;
    private RecipeCustomFields recipeCustomFields;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        tmpDir = FileSystemUtils.createTempDir(getName(), ".tmp");
        recipeCustomFields = new RecipeCustomFields(tmpDir);
    }

    @Override
    protected void tearDown() throws Exception
    {
        FileSystemUtils.rmdir(tmpDir);
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
        recipeCustomFields.store(asMap(asPair("field1", "original value")));
        Map<String, String> fields = recipeCustomFields.load();
        assertEquals(1, fields.size());
        assertEquals("original value", fields.get("field1"));

        recipeCustomFields.store(asMap(asPair("field1", "new value"), asPair("field2", "value")));
        fields = recipeCustomFields.load();
        assertEquals(2, fields.size());
        assertEquals("new value", fields.get("field1"));
        assertEquals("value", fields.get("field2"));
    }

    private void roundTrip(Map<String, String> fields)
    {
        recipeCustomFields.store(fields);
        Map<String, String> loaded = recipeCustomFields.load();
        assertNotSame(fields, loaded);
        assertEquals(fields, loaded);
    }
}
