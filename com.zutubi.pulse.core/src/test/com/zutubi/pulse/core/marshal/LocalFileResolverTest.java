package com.zutubi.pulse.core.marshal;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.io.IOUtils;

import java.io.File;

public class LocalFileResolverTest extends PulseTestCase
{
    private static final String FILENAME = "f";
    private static final String DIRECTORY = "nested";
    private static final String CONTENT_SIMPLE = "content";
    private static final String CONTENT_NESTED = "nested content";

    private File tempDir;
    private LocalFileResolver resolver;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        tempDir = createTempDirectory();
        resolver = new LocalFileResolver(tempDir);
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        FileSystemUtils.rmdir(tempDir);
    }

    public void testSimple() throws Exception
    {
        File f = new File(tempDir, FILENAME);
        FileSystemUtils.createFile(f, CONTENT_SIMPLE);
        assertEquals(CONTENT_SIMPLE, IOUtils.inputStreamToString(resolver.resolve(FILENAME)));
    }

    public void testNested() throws Exception
    {
        File dir = new File(tempDir, DIRECTORY);
        assertTrue(dir.mkdir());
        File nested = new File(dir, FILENAME);
        FileSystemUtils.createFile(nested, CONTENT_NESTED);
        assertEquals(CONTENT_NESTED, IOUtils.inputStreamToString(resolver.resolve(PathUtils.getPath(DIRECTORY, FILENAME))));
    }
}
