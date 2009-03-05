package com.zutubi.pulse.core.test.api;

import com.zutubi.pulse.core.util.ZipUtils;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.junit.ZutubiTestCase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.zip.ZipInputStream;

/**
 * Base class for test cases.
 */
public abstract class PulseTestCase extends ZutubiTestCase
{
    public PulseTestCase()
    {
    }

    public PulseTestCase(String name)
    {
        super(name);
    }

    /**
     * Unzips an archive named after the current class and test method to the
     * given directory.  Equivalent to unzipInput(getName(), toDir).
     *
     * @see #unzipInput(String, java.io.File)
     *
     * @param toDir directory to extract the archive to, should already exist
     * @throws java.io.IOException if there is a problem locating or extracting the
     *         archive
     */
    public  void unzipInput(File toDir) throws IOException
    {
        unzipInput(getName(), toDir);
    }

    /**
     * Unpacks a test data zip by locating it on the classpath and exploding it
     * into the specified directory.  The zip input stream is located using
     * {@link #getInput(String, String)} with "zip" passed as the extension.
     * This effectively means the zip should be alongside the current class in
     * the classpath with name &lt;simple classname&gt;.name.zip.
     *
     * @param name  name of the zip archive, appended to the class name
     * @param toDir directory to extract the archive to, should already exist
     * @throws IOException if there is a problem locating or extracting the
     *         archive
     */
    public  void unzipInput(String name, File toDir) throws IOException
    {
        ZipInputStream is = null;
        try
        {
            is = new ZipInputStream(getInput(name, "zip"));
            ZipUtils.extractZip(is, toDir);
        }
        finally
        {
            IOUtils.close(is);
        }
    }
}
