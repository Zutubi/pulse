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

public class DeleteMetaPropertyRecordUpgraderTest extends PulseTestCase
{
    private static final String PROPERTY_NAME = "some name here";

    private DeleteMetaPropertyRecordUpgrader upgrader = new DeleteMetaPropertyRecordUpgrader(PROPERTY_NAME);

    public void testMetaPropertyExists()
    {
        MutableRecord record = new MutableRecordImpl();
        record.putMeta(PROPERTY_NAME, "anything");
        upgrader.upgrade(null, record);
        assertNull(record.getMeta(PROPERTY_NAME));
    }

    public void testMetaPropertyDoesNotExist()
    {
        MutableRecord record = new MutableRecordImpl();
        upgrader.upgrade(null, record);
        assertNull(record.getMeta(PROPERTY_NAME));
    }

    public void testOtherMetaPropertyUntouched()
    {
        final String OTHER_NAME = PROPERTY_NAME + "x";
        final String VALUE = "anything";

        MutableRecord record = new MutableRecordImpl();
        record.putMeta(OTHER_NAME, VALUE);
        upgrader.upgrade(null, record);
        assertEquals(VALUE, record.getMeta(OTHER_NAME));
    }

    public void testNonMetaPropertyUntouched()
    {
        final String VALUE = "anything";

        MutableRecord record = new MutableRecordImpl();
        record.put(PROPERTY_NAME, VALUE);
        upgrader.upgrade(null, record);
        assertEquals(VALUE, record.get(PROPERTY_NAME));
    }
}