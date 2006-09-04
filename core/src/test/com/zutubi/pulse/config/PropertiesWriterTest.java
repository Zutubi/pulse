package com.zutubi.pulse.config;

import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * <class-comment/>
 */
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

        tmpDir = FileSystemUtils.createTempDirectory(getClass().getName(), getName());

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
        assertStreamsEqual(
                getClass().getResourceAsStream(getClass().getSimpleName() + "." + getName() + ".expected.properties"),
                new FileInputStream(config)
        );

        //for the paranoid: verify that the when reading the config we end up with the same content as we set.
    }

//    public void testUpdateExistingMultilineProperties() throws IOException
//    {
//        //TODO:
//    }
}
