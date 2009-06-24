package com.zutubi.pulse.core.dependency;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import static com.zutubi.pulse.core.dependency.RepositoryAttributePredicates.attributeEquals;
import com.zutubi.util.FileSystemUtils;

import java.io.File;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import static java.util.Arrays.asList;

public class RepositoryAttributesTest extends PulseTestCase
{
    private RepositoryAttributes attributes;
    private File tmp;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = FileSystemUtils.createTempDir();
        attributes = new RepositoryAttributes(tmp);
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
        assertEquals(paths, asList("path1/child", "path2", "path2/child"));
    }
}
