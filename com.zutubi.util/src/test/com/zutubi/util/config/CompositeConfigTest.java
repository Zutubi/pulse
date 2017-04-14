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

package com.zutubi.util.config;

import junit.framework.Assert;
import com.zutubi.util.junit.ZutubiTestCase;

public class CompositeConfigTest extends ZutubiTestCase
{
    public void testNoDelegates()
    {
        // check that all methods behave as expected.
        CompositeConfig config = new CompositeConfig();
        Assert.assertFalse(config.hasProperty("some.property"));
        Assert.assertNull(config.getProperty("some.property"));
        Assert.assertFalse(config.isWritable());

        // unable to store content.
        config.setProperty("some.property", "value");
        Assert.assertNull(config.getProperty("some.property"));
    }

    public void testOneDelegate()
    {
        PropertiesConfig delegate = new PropertiesConfig();

        // check that all methods behave as expected.
        CompositeConfig config = new CompositeConfig(delegate);
        Assert.assertEquals(delegate.hasProperty("some.property"), config.hasProperty("some.property"));
        Assert.assertEquals(delegate.getProperty("some.property"), config.getProperty("some.property"));
        Assert.assertEquals(delegate.isWritable(), config.isWritable());

        // unable to store content.
        config.setProperty("some.property", "value");
        Assert.assertEquals(delegate.getProperty("some.property"), config.getProperty("some.property"));
    }
}
