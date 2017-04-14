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

package com.zutubi.pulse.master.hibernate;

import com.zutubi.pulse.core.test.api.PulseTestCase;

import java.io.IOException;
import java.util.Arrays;

public class MutableConfigurationTest extends PulseTestCase
{
    public void testPropertiesCopied()
    {
        MutableConfiguration config = new MutableConfiguration();
        config.setProperty("a", "b");

        MutableConfiguration copy = config.copy();
        assertEquals(config.getProperty("a"), copy.getProperty("a"));
    }

    public void testMappingsCopied() throws IOException
    {
        MutableConfiguration config = new MutableConfiguration();
        assertNull(config.getClassMapping("types"));
        
        config.addClassPathMappings(Arrays.asList("com/zutubi/pulse/master/transfer/Schema.hbm.xml"));
        config.buildMappings();
        assertNotNull(config.getClassMapping("types"));

        MutableConfiguration copy = config.copy();
        copy.buildMappings();
        assertNotNull(copy.getClassMapping("types"));
    }
}
