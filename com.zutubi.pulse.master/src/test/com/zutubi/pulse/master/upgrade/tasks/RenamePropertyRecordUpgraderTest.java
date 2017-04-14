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

import com.zutubi.pulse.core.test.api.PulseTestCase;
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
