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

import com.zutubi.util.io.IOUtils;
import junit.framework.Assert;
import com.zutubi.util.junit.ZutubiTestCase;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class FileConfigTest extends ZutubiTestCase
{
    private Config config = null;
    private File testProperties;

    public void setUp() throws Exception
    {
        super.setUp();

        // temporary properties file
        testProperties = File.createTempFile(FileConfigTest.class.getName(), ".properties");

        Properties defaults = new Properties();
        defaults.put("key", "value");
        IOUtils.write(defaults, testProperties);

        // add setup code here.
        config = new FileConfig(testProperties);
    }

    public void tearDown() throws Exception
    {
        // add tear down code here.
        if (!testProperties.delete())
        {
            throw new IOException("");
        }

        super.tearDown();
    }

    public void testGetProperties() throws Exception
    {
        Assert.assertEquals("value", config.getProperty("key"));
    }

    public void testSetProperties() throws Exception
    {
        config.setProperty("key", "anotherValue");
        Assert.assertEquals("anotherValue", config.getProperty("key"));

        Properties props = IOUtils.read(testProperties);
        Assert.assertEquals("anotherValue", props.getProperty("key"));
        Assert.assertEquals(1, props.size());
    }

    public void testCreationOfPropertiesFile() throws Exception
    {
        Assert.assertTrue(testProperties.delete());

        config.setProperty("key", "anotherValue");
        Assert.assertEquals("anotherValue", config.getProperty("key"));

        Properties props = IOUtils.read(testProperties);
        Assert.assertEquals("anotherValue", props.getProperty("key"));
        Assert.assertEquals(1, props.size());
    }
}
