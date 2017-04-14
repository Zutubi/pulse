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

package com.zutubi.util;

import com.zutubi.util.junit.ZutubiTestCase;

public class EnumUtilsTest extends ZutubiTestCase
{
    public enum SampleEnum
    {
        NAME, NAME_WITH_UNDERSCORES
    }

    public void testToPrettyString()
    {
        assertEquals("name", EnumUtils.toPrettyString(SampleEnum.NAME));
        assertEquals("name with underscores", EnumUtils.toPrettyString(SampleEnum.NAME_WITH_UNDERSCORES));
    }

    public void testToString()
    {
        assertEquals("name", EnumUtils.toString(SampleEnum.NAME));
        assertEquals("namewithunderscores", EnumUtils.toString(SampleEnum.NAME_WITH_UNDERSCORES));
    }

    public void testFromPrettyString()
    {
        assertEquals("NAME", EnumUtils.fromPrettyString("name"));
        assertEquals("NAME_WITH_UNDERSCORES", EnumUtils.fromPrettyString("name with underscores"));
    }

    public void testEnumFromPrettyString()
    {
        assertEquals(SampleEnum.NAME, EnumUtils.fromPrettyString(SampleEnum.class, "name"));        
        assertEquals(SampleEnum.NAME_WITH_UNDERSCORES, EnumUtils.fromPrettyString(SampleEnum.class, "name with underscores"));
    }
}
