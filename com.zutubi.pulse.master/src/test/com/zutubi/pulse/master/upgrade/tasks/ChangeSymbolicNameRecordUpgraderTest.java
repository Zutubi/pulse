package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.tove.type.record.MutableRecordImpl;

public class ChangeSymbolicNameRecordUpgraderTest extends PulseTestCase
{
    private static final String OLD_NAME = "old";
    private static final String NEW_NAME = "new";
    private static final String OTHER_NAME = "other";

    private ChangeSymbolicNameRecordUpgrader upgrader = new ChangeSymbolicNameRecordUpgrader(OLD_NAME, NEW_NAME);

    public void testHasOldName()
    {
        MutableRecordImpl record = new MutableRecordImpl();
        record.setSymbolicName(OLD_NAME);
        upgrader.upgrade(null, record);
        assertEquals(NEW_NAME, record.getSymbolicName());
    }
    
    public void testDoesNotHaveOldName()
    {
        MutableRecordImpl record = new MutableRecordImpl();
        record.setSymbolicName(OTHER_NAME);
        upgrader.upgrade(null, record);
        assertEquals(OTHER_NAME, record.getSymbolicName());
    }
}
