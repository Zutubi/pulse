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
