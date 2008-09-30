package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.MutableRecordImpl;

public class RenamePropertyRecordUpgraderTest extends PulseTestCase
{
    private static final String OLD_NAME = "boring name";
    private static final String NEW_NAME = "shiny name";
    private static final String VALUE = "anything";

    private RenamePropertyRecordUpgrader upgrader = new RenamePropertyRecordUpgrader(OLD_NAME, NEW_NAME);

    public void testPropertyExists()
    {
        MutableRecord record = new MutableRecordImpl();
        record.put(OLD_NAME, VALUE);
        upgrader.upgrade(null, record);
        assertNull(record.get(OLD_NAME));
        assertEquals(VALUE, record.get(NEW_NAME));
    }

    public void testPropertyDoesNotExist()
    {
        MutableRecord record = new MutableRecordImpl();
        upgrader.upgrade(null, record);
        assertNull(record.get(OLD_NAME));
        assertNull(record.get(NEW_NAME));
    }

    public void testPropertyExistsButSoDoesNewProperty()
    {
        MutableRecord record = new MutableRecordImpl();
        record.put(OLD_NAME, VALUE);
        record.put(NEW_NAME, "anything");
        try
        {
            upgrader.upgrade(null, record);
            fail("Should not be able to rename over an existing property");
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage().contains("already contains a property"));
        }
    }

    public void testPropertyIsNonSimple()
    {
        MutableRecord record = new MutableRecordImpl();
        record.put(OLD_NAME, new MutableRecordImpl());
        try
        {
            upgrader.upgrade(null, record);
            fail("Should not be able to rename a non-simple property");
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage().contains("non-simple property"));
        }
    }
}
