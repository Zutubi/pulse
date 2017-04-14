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
