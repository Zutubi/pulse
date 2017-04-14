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

package com.zutubi.pulse.core.model;

import com.google.common.collect.ImmutableMap;
import com.zutubi.pulse.core.test.api.PulseTestCase;

import java.io.File;
import java.util.Map;

public class ResultCustomFieldsTest extends PulseTestCase
{
    private static final String PUNCTUATION = "`~!@#$%^&*()-_=+\\|]}[{'\";:/?.>,<";

    private File tmpDir;
    private ResultCustomFields resultCustomFields;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        tmpDir = createTempDirectory();
        resultCustomFields = new ResultCustomFields(tmpDir);
    }

    @Override
    protected void tearDown() throws Exception
    {
        removeDirectory(tmpDir);
        super.tearDown();
    }

    public void testSimpleProperties()
    {
        roundTrip(ImmutableMap.of("field1", "value1", "field2", "value2"));
    }

    public void testExoticPropertyName()
    {
        roundTrip(ImmutableMap.of(PUNCTUATION, "value"));
    }

    public void testExoticPropertyValue()
    {
        roundTrip(ImmutableMap.of("field", PUNCTUATION));
    }

    public void testLoadAfterUpdate()
    {
        resultCustomFields.store(ImmutableMap.of("field1", "original value"));
        Map<String, String> fields = resultCustomFields.load();
        assertEquals(1, fields.size());
        assertEquals("original value", fields.get("field1"));

        resultCustomFields.store(ImmutableMap.of("field1", "new value", "field2", "value"));
        fields = resultCustomFields.load();
        assertEquals(2, fields.size());
        assertEquals("new value", fields.get("field1"));
        assertEquals("value", fields.get("field2"));
    }

    private void roundTrip(Map<String, String> fields)
    {
        resultCustomFields.store(fields);
        Map<String, String> loaded = resultCustomFields.load();
        assertNotSame(fields, loaded);
        assertEquals(fields, loaded);
    }
}
