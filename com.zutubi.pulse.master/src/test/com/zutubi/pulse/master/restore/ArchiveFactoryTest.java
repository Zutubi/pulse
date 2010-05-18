package com.zutubi.pulse.master.restore;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class ArchiveFactoryTest extends PulseTestCase
{
    private ArchiveFactory factory;

    private File tmp;
    private File exportDir;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = createTempDirectory();
        factory = new ArchiveFactory();
        exportDir = new File(tmp, "export");
        factory.setTmpDirectory(tmp);
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tmp);
        tmp = null;
        exportDir = null;
        
        super.tearDown();
    }

    public void testCreateNewExpandedDirectoryArchive() throws ArchiveException
    {
        // specify the location for new archives.

        Archive createdArchive = factory.createArchive();

        assertNotNull(createdArchive.getCreated());
        assertNotNull(createdArchive.getVersion());       

        assertNotNull(createdArchive.getBase());
        assertTrue(createdArchive.getBase().exists());

        // check archives base directories are created in the tmp directory as configured..
        assertEquals(tmp, createdArchive.getBase().getParentFile());
        assertNull(createdArchive.getOriginal());
    }

    public void testExportArchive() throws ArchiveException
    {
        Archive archive = factory.createArchive();
        File exportedZipFile = factory.exportArchive(archive, exportDir);

        // ensure that the exported archives are written to the requested directory.
        assertNotNull(exportedZipFile);
        assertEquals(exportDir, exportedZipFile.getParentFile());
    }

    public void testCorrectNameUsedForArchive() throws ArchiveException
    {
        factory.setArchiveNameGenerator(new ArchiveNameGenerator()
        {
            public String newName(File target)
            {
                return "sample";
            }
            public boolean matches(String name)
            {
                return name.equals("sample");
            }

            public int compareTo(String nameA, String nameB)
            {
                return 0;
            }
        });
        Archive archive = factory.createArchive();

        File exportedZipFile = factory.exportArchive(archive, exportDir);

        assertEquals("sample", exportedZipFile.getName());
    }

    public void testArchivingDataRoundTrip() throws ArchiveException, IOException
    {
        Archive archive = factory.createArchive();
        File archiveBase = archive.getBase();

        createSampleDataFile(new File(archiveBase, "sample.txt"));

        File zip = factory.exportArchive(archive, exportDir);

        Archive importedArchive = factory.importArchive(zip);

        assertNotSame(archive.getBase(), importedArchive.getBase());
        assertTrue(new File(importedArchive.getBase(), "sample.txt").isFile());
    }
    
    private void createSampleDataFile(File dataFile) throws IOException
    {
        if (!dataFile.exists() && !dataFile.createNewFile())
        {
            throw new IOException();
        }
        IOUtils.write(new Properties(), dataFile);
    }
}
