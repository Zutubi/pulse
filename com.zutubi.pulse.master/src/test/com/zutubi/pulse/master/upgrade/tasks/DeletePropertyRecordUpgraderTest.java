package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.core.test.PulseTestCase;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.MutableRecordImpl;

public class DeletePropertyRecordUpgraderTest extends PulseTestCase
{
    private static final String PROPERTY_NAME = "some name here";

    private DeletePropertyRecordUpgrader upgrader = new DeletePropertyRecordUpgrader(PROPERTY_NAME);

    public void testPropertyExists()
    {
        MutableRecord record = new MutableRecordImpl();
        record.put(PROPERTY_NAME, "anything");
        upgrader.upgrade(null, record);
        assertNull(record.get(PROPERTY_NAME));
    }

    public void testPropertyDoesNotExist()
    {
        MutableRecord record = new MutableRecordImpl();
        upgrader.upgrade(null, record);
        assertNull(record.get(PROPERTY_NAME));
    }

    public void testPropertyNotSimple()
    {
        MutableRecord record = new MutableRecordImpl();
        record.put(PROPERTY_NAME, new MutableRecordImpl());
        try
        {
            upgrader.upgrade(null, record);
            fail("Should not be able to delete a non-simple value");
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage().contains("non-simple value"));
        }
    }
}
