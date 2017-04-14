/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.upgrade.tasks;

import com.google.common.base.Function;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.tove.type.record.MutableRecordImpl;

public class EditPropertyRecordUpgraderTest extends PulseTestCase
{
    private static final String PROPERTY_NAME = "property";
    private static final String PROPERTY_VALUE = "test value";
    private static final String PROPERTY_VALUE_EDITED = "edited value";
    private static final String SCOPE = "scope";
    private static final String PATH = SCOPE + "/path";

    public void testSimpleEdit()
    {
        EditPropertyRecordUpgrader upgrader = new EditPropertyRecordUpgrader(PROPERTY_NAME, new Function<Object, Object>()
        {
            public Object apply(Object o)
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
        EditPropertyRecordUpgrader upgrader = new EditPropertyRecordUpgrader(PROPERTY_NAME, new Function<Object, Object>()
        {
            public Object apply(Object o)
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
        EditPropertyRecordUpgrader upgrader = new EditPropertyRecordUpgrader(PROPERTY_NAME, new Function<Object, Object>()
        {
            public Object apply(Object o)
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