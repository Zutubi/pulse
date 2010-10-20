package com.zutubi.util.io;

import com.zutubi.util.FileSystemUtils;
import junit.framework.AssertionFailedError;
import com.zutubi.util.junit.ZutubiTestCase;

import java.io.*;
import java.util.Properties;

public class PropertiesWriterTest extends ZutubiTestCase
{
    private File tmpDir = null;
    private File config;

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

    protected static void assertStreamsEqual(InputStream is1, InputStream is2) throws IOException
    {
        try
        {
            BufferedReader rs1 = new BufferedReader(new InputStreamReader(is1));
            BufferedReader rs2 = new BufferedReader(new InputStreamReader(is2));
            while (true)
            {
                String line1 = rs1.readLine();
                String line2 = rs2.readLine();

                if (line1 == null)
                {
                    if (line2 == null)
                    {
                        return;
                    }
                    throw new AssertionFailedError("Contents of stream 1 differ from contents of stream 2. ");
                }
                else
                {
                    if (line2 == null)
                    {
                        throw new AssertionFailedError("Contents of stream 1 differ from contents of stream 2. ");
                    }
                    assertEquals(line1, line2);
                }
            }
        }
        finally
        {
            // close the streams for convenience.
            IOUtils.close(is1);
            IOUtils.close(is2);
        }
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
