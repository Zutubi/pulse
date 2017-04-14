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

package com.zutubi.pulse.core.dependency;

import static com.zutubi.pulse.core.dependency.RepositoryAttributePredicates.attributeEquals;
import com.zutubi.pulse.core.test.api.PulseTestCase;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RepositoryAttributesTest extends PulseTestCase
{
    private RepositoryAttributes attributes;
    private File tmp;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = createTempDirectory();
        attributes = newAttributes();
    }

    private RepositoryAttributes newAttributes() throws IOException
    {
        RepositoryAttributes attributes = new RepositoryAttributes(tmp);
        attributes.init();
        return attributes;
    }

    @Override
    protected void tearDown() throws Exception
    {
        removeDirectory(tmp);

        super.tearDown();
    }

    public void testAddAttribute()
    {
        attributes.addAttribute("mypath", "name", "value");
        assertEquals("value", attributes.getAttribute("mypath", "name"));
    }

    public void testRemoveAttribute()
    {
        assertFalse(attributes.removeAttribute("path", "name"));

        attributes.addAttribute("path", "name", "value");
        assertNotNull(attributes.getAttribute("path", "name"));

        assertTrue(attributes.removeAttribute("path", "name"));
        assertNull(attributes.getAttribute("path", "name"));
    }

    public void testGetAttributes()
    {
        attributes.addAttribute("path", "name1", "value1");
        attributes.addAttribute("path", "name2", "value2");

        Map<String, String> attributes = this.attributes.getMergedAttributes("path");
        assertTrue(attributes.containsKey("name1"));
        assertTrue(attributes.containsKey("name2"));
    }

    public void testAttributesOverrideByPath()
    {
        attributes.addAttribute("path", "name", "value1");
        attributes.addAttribute("path/child", "name", "value2");

        assertEquals("value1", attributes.getMergedAttributes("path").get("name"));
        assertEquals("value2", attributes.getMergedAttributes("path/child").get("name"));
        assertEquals("value2", attributes.getMergedAttributes("path/child/grandchild").get("name"));
    }

    public void testAttributeEqualsPredicate()
    {
        attributes.addAttribute("path1", "name", "value1");
        attributes.addAttribute("path1/child", "name", "value2");
        attributes.addAttribute("path2", "name", "value2");
        attributes.addAttribute("path2/child", "name", "value2");

        List<String> paths = attributes.getPaths(attributeEquals("name", "value2"));
        assertEquals(3, paths.size());
        assertTrue(paths.contains("path1/child"));
        assertTrue(paths.contains("path2/child"));
        assertTrue(paths.contains("path2"));
    }

    public void testPersistentAdd() throws IOException
    {
        attributes.addAttribute("path1", "name", "value1");

        assertTrue(new File(tmp, "path1/" + RepositoryAttributes.ATTRIBUTE_FILE_NAME).isFile());

        attributes = newAttributes();
        assertEquals("value1", attributes.getAttribute("path1", "name"));

        List<String> paths = attributes.getPaths(attributeEquals("name", "value1"));
        assertEquals(1, paths.size());
        assertTrue(paths.contains("path1"));
    }

    public void testPersistentRemove() throws IOException
    {
        attributes.addAttribute("path1", "name1", "value1");
        attributes.addAttribute("path1", "name2", "value2");

        attributes = newAttributes();
        assertTrue(attributes.removeAttribute("path1", "name1"));

        attributes = newAttributes();
        assertEquals(null, attributes.getAttribute("path1", "name1"));
    }
}
