package com.zutubi.pulse.master.restore;

import com.zutubi.pulse.core.test.PulseTestCase;
import com.zutubi.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class UniqueDatestampedNameGeneratorTest extends PulseTestCase
{
    private File tmp;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = FileSystemUtils.createTempDir();
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tmp);
        tmp = null;

        super.tearDown();
    }

    public void testGenerateMatchesRoundTrip()
    {
        UniqueDatestampedNameGenerator generator = new UniqueDatestampedNameGenerator();
        assertTrue(generator.matches(generator.newName(tmp)));
    }

    public void testUniqueNameGenerator() throws IOException
    {
        UniqueDatestampedNameGenerator generator = new UniqueDatestampedNameGenerator();
        generator.setTime(new UniqueDatestampedNameGenerator.Clock()
        {
            public Date getDate()
            {
                return new Date(0);
            }
        });

        assertEquals("archive-1970-01-01_10-00-00.zip", generator.newName(tmp));
        assertTrue(new File(tmp, "archive-1970-01-01_10-00-00.zip").createNewFile());
        assertEquals("archive-1970-01-01_10-00-00_1.zip", generator.newName(tmp));
    }

    public void testMatchesPicksUpMultipleCandidateNames()
    {
        UniqueDatestampedNameGenerator generator = new UniqueDatestampedNameGenerator();
        assertTrue(generator.matches("archive-1970-01-01_10-00-00.zip"));
        assertTrue(generator.matches("archive-1970-01-01_10-00-00_1.zip"));
        assertTrue(generator.matches("archive-1970-01-01_10-00-00_2.zip"));        
    }

    public void testGeneratedNameIsHigherThanAllExistingNames() throws IOException
    {
        UniqueDatestampedNameGenerator generator = new UniqueDatestampedNameGenerator();
        generator.setTime(new UniqueDatestampedNameGenerator.Clock()
        {
            public Date getDate()
            {
                return new Date(0);
            }
        });

        assertTrue(new File(tmp, "archive-1970-01-01_10-00-00_5.zip").createNewFile());
        assertEquals("archive-1970-01-01_10-00-00_6.zip", generator.newName(tmp));
    }
}