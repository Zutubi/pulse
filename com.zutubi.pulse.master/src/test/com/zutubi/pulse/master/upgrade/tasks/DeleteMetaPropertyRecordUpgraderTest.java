package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.MutableRecordImpl;

public class DeleteMetaPropertyRecordUpgraderTest extends PulseTestCase
{
    private static final String PROPERTY_NAME = "some name here";

    private DeleteMetaPropertyRecordUpgrader upgrader = new DeleteMetaPropertyRecordUpgrader(PROPERTY_NAME);

    public void testPropertyExists()
    {
        MutableRecord record = new MutableRecordImpl();
        record.putMeta(PROPERTY_NAME, "anything");
        upgrader.upgrade(null, record);
        assertNull(record.getMeta(PROPERTY_NAME));
    }

    public void testPropertyDoesNotExist()
    {
        MutableRecord record = new MutableRecordImpl();
        upgrader.upgrade(null, record);
        assertNull(record.getMeta(PROPERTY_NAME));
    }
}