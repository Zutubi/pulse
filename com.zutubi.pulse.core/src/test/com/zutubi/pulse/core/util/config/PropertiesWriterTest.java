package com.zutubi.pulse.core.util.config;

import com.zutubi.pulse.core.test.PulseTestCase;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesWriterTest extends PulseTestCase
{
    private File tmpDir = null;
    private File config;

    public PropertiesWriterTest()
    {
    }

    public PropertiesWriterTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        tmpDir = FileSystemUtils.createTempDir(getClass().getName(), getName());

        // copy contents of original file into the temporary directory.
        config = new File(tmpDir, "tmp.config.properties");
        assertTrue(config.createNewFile());

        IOUtils.joinStreams(
                getClass().getResourceAsStream(getClass().getSimpleName() + "." + getName() + ".original.properties"),
                new FileOutputStream(config), true
        );
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tmpDir);
        tmpDir = null;

        super.tearDown();
    }

    public void testUpdateExistingProperties() throws IOException
    {
        // write an update to the some.property value
        Properties props = new Properties();
        props.setProperty("simple.property", "a new value");
        props.setProperty("simple.property.2", "another new value");
        props.setProperty("simple.property.3", "c\\b\\d");

        PropertiesWriter writer = new PropertiesWriter();
        writer.write(config, props);

        // assert the file is as expected.
        assertAsExpected();
        
        Properties written = IOUtils.read(config);
        assertEquals(props.getProperty("simple.property"), written.getProperty("simple.property"));
        assertEquals(props.getProperty("simple.property.2"), written.getProperty("simple.property.2"));
        assertEquals(props.getProperty("simple.property.3"), written.getProperty("simple.property.3"));
    }

    private void assertAsExpected() throws IOException
    {
        assertStreamsEqual(
                getClass().getResourceAsStream(getClass().getSimpleName() + "." + getName() + ".expected.properties"),
                new FileInputStream(config)
        );
    }

    public void testUpdateExistingMultilineProperties() throws IOException
    {
        Properties props = new Properties();
        props.setProperty("multiline.property", "a new value");
        props.setProperty("multiline.property.2", "a new value with\nnew lines");

        PropertiesWriter writer = new PropertiesWriter();
        writer.write(config, props);

        assertAsExpected();

        Properties written = IOUtils.read(config);
        assertEquals(props.getProperty("multiline.property"), written.getProperty("multiline.property"));
        assertEquals(props.getProperty("multiline.property.2"), written.getProperty("multiline.property.2"));
    }
}
