package com.zutubi.pulse.master;

import com.zutubi.pulse.core.test.PulseTestCase;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.IOException;

public class DefaultRecipeLoggerTest extends PulseTestCase
{
    private File tmpDir;
    private File logFile;
    private RecipeLogger logger;


    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        tmpDir = FileSystemUtils.createTempDir(getName(), ".tmp");
        logFile = new File(tmpDir, "file.log");
        logger = new DefaultRecipeLogger(logFile);
        logger.prepare();
    }

    @Override
    protected void tearDown() throws Exception
    {
        logger.done();
        removeDirectory(tmpDir);
        super.tearDown();
    }
    
    public void testOutputFlushed() throws IOException
    {
        final String SHORT_STRING = "1";

        logger.log(SHORT_STRING.getBytes());
        assertEquals(SHORT_STRING, IOUtils.fileToString(logFile));
    }
}
