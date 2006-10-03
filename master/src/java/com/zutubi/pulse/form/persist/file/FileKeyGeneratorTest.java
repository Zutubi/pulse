package com.zutubi.pulse.form.persist.file;

import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.form.persist.PersistenceException;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * <class-comment/>
 */
public class FileKeyGeneratorTest extends PulseTestCase
{
    private FileKeyGenerator keyGenerator;
    private File tmpDir;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmpDir = FileSystemUtils.createTempDirectory(FileKeyGenerator.class.getName(), "tmp");

        keyGenerator = new FileKeyGenerator(tmpDir);

    }

    protected void tearDown() throws Exception
    {
        keyGenerator = null;

        removeDirectory(tmpDir);

        super.tearDown();
    }

    public void testKeyGenerationIsSequentialWithOutGaps() throws PersistenceException
    {
        for (long j = 1; j < 1011; j++)
        {
            assertEquals(j, keyGenerator.generate(String.class));
        }
    }

    public void testDistinctObjectsHaveDistinctKeys() throws PersistenceException
    {
        assertEquals(1L, keyGenerator.generate(String.class));
        assertEquals(2L, keyGenerator.generate(String.class));
        assertEquals(1L, keyGenerator.generate(Object.class));
        assertEquals(2L, keyGenerator.generate(Object.class));
        assertEquals(3L, keyGenerator.generate(Object.class));
        assertEquals(3L, keyGenerator.generate(String.class));
    }

    public void testThatKeysAreStoredInFile() throws PersistenceException, IOException
    {
        assertEquals(1L, keyGenerator.generate(String.class));
        assertEquals(1L, keyGenerator.generate(Object.class));

        Properties props = IOUtils.read(new File(tmpDir, "keys.properties"));
        assertEquals("20", props.getProperty("java.lang.String"));
        assertEquals("20", props.getProperty("java.lang.Object"));
        assertFalse(props.containsKey(Long.class));
    }

    public void testGeneratorAcrossRestart() throws PersistenceException
    {
        assertEquals(1L, keyGenerator.generate(String.class));

        // simulate a system restart.
        keyGenerator = new FileKeyGenerator(tmpDir);

        assertEquals(21L, keyGenerator.generate(String.class));
    }
}
