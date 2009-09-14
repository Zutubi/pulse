package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.util.UnaryFunction;

public class EditPropertyRecordUpgraderTest extends PulseTestCase
{
    private static final String PROPERTY_NAME = "property";
    private static final String PROPERTY_VALUE = "test value";
    private static final String PROPERTY_VALUE_EDITED = "edited value";
    private static final String SCOPE = "scope";
    private static final String PATH = SCOPE + "/path";

    public void testSimpleEdit()
    {
        EditPropertyRecordUpgrader upgrader = new EditPropertyRecordUpgrader(PROPERTY_NAME, new UnaryFunction<Object, Object>()
        {
            public Object process(Object o)
            {
                return PROPERTY_VALUE_EDITED;
            }
        });
        
        MutableRecordImpl mutable = new MutableRecordImpl();
        mutable.put(PROPERTY_NAME, PROPERTY_VALUE);
        upgrader.upgrade(PATH, mutable);
        assertEquals(PROPERTY_VALUE_EDITED, mutable.get(PROPERTY_NAME));
    }

    public void testAdd()
    {
        EditPropertyRecordUpgrader upgrader = new EditPropertyRecordUpgrader(PROPERTY_NAME, new UnaryFunction<Object, Object>()
        {
            public Object process(Object o)
            {
                return PROPERTY_VALUE;
            }
        });

        MutableRecordImpl mutable = new MutableRecordImpl();
        upgrader.upgrade(PATH, mutable);
        assertEquals(PROPERTY_VALUE, mutable.get(PROPERTY_NAME));
    }

    public void testRemove()
    {
        EditPropertyRecordUpgrader upgrader = new EditPropertyRecordUpgrader(PROPERTY_NAME, new UnaryFunction<Object, Object>()
        {
            public Object process(Object o)
            {
                return null;
            }
        });

        MutableRecordImpl mutable = new MutableRecordImpl();
        mutable.put(PROPERTY_NAME, PROPERTY_VALUE);
        upgrader.upgrade(PATH, mutable);
        assertNull(mutable.get(PROPERTY_NAME));
    }
}