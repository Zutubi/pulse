package com.zutubi.util;

import com.zutubi.util.io.IOUtils;
import com.zutubi.util.junit.ZutubiTestCase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ZipUtilsTest extends ZutubiTestCase
{
    private File tmpDir;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        tmpDir = createTempDirectory();
    }

    @Override
    protected void tearDown() throws Exception
    {
        removeDirectory(tmpDir);

        super.tearDown();
    }

    public void testCompressUncompressEmpty() throws IOException
    {
        File f = File.createTempFile(getName(), ".tmp", tmpDir);
        doCompressRoundTrip(f);
    }

    public void testCompressUncompressSmall() throws IOException
    {
        File f = File.createTempFile(getName(), ".tmp", tmpDir);
        FileSystemUtils.createFile(f, "a little data");
        doCompressRoundTrip(f);
    }

    public void testCompressUncompressLarge() throws IOException
    {
        File f = File.createTempFile(getName(), ".tmp", tmpDir);
        String random = RandomUtils.randomString(1024);
        byte[] randomBytes = random.getBytes();
        FileOutputStream os = new FileOutputStream(f);
        try
        {
            for (int i = 0; i < 1024; i++)
            {
                os.write(randomBytes);
            }
        }
        finally
        {
            IOUtils.close(os);
        }

        doCompressRoundTrip(f);
    }

    public void testCompressNonExistent()
    {
        try
        {
            ZipUtils.compressFile(new File("idontexist"), new File("dummy"));
            fail("Cannot compress non-existent file");
        }
        catch (IOException e)
        {
            // OS-specific message
        }
    }

    public void testUncompressNonExistent()
    {
        try
        {
            ZipUtils.uncompressFile(new File("idontexist"), new File("dummy"));
            fail("Cannot uncompress non-existent file");
        }
        catch (IOException e)
        {
            // OS-specific message
        }
    }

    public void testCompressUncompressOutFilesExist() throws IOException
    {
        File in = File.createTempFile(getName(), ".tmp", tmpDir);
        File compressed = new File(in.getAbsolutePath() + ".compressed");
        File uncompressed = new File(in.getAbsolutePath() + ".uncompressed");
        FileSystemUtils.createFile(in, "a little data");
        FileSystemUtils.createFile(compressed, "in the way");
        FileSystemUtils.createFile(uncompressed, "also in the way");

        doCompressRoundTrip(in, compressed, uncompressed);
    }

    private void doCompressRoundTrip(File original) throws IOException
    {
        doCompressRoundTrip(original, new File(original.getAbsolutePath() + ".compressed"), new File(original.getAbsolutePath() + ".uncompressed"));
    }

    private void doCompressRoundTrip(File original, File compressed, File uncompressed) throws IOException
    {
        ZipUtils.compressFile(original, compressed);
        ZipUtils.uncompressFile(compressed, uncompressed);
        IOAssertions.assertFilesEqual(original, uncompressed);
    }
}
