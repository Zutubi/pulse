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
     * @throws IOException if there is a problem locating or extracting the
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

    /**
     * Retrieves a file object for test data on the classpath based on the
     * test name and given extension.  Equivalent to getInputFile(getName(), extension).
     *
     * @see #getInputFile(String, String)
     * @see #copyInputToDirectory(String, String, java.io.File)
     *
     * @param extension the extension of the test data file
     * @return a file object pointed to the input data: note that as this file
     *         is from the classpath it may not always be possible to use it
     *         directly
     */
    public File getInputFile(String extension)
    {
        return getInputFile(getName(), extension);
    }

    /**
     * Retrieves a file object for test data on the classpath based on the
     * given name and given extension.  The data file will be located using
     * {@link #getInputURL(String, String)} and a file generated from the URL.
     * This effectively means the file should be alongside the test class on
     * the classpath with name &lt;simple classname&gt;.name.extension.
     *
     * @see #getInputURL(String, String)
     * @see #copyInputToDirectory(String, String, java.io.File)
     *
     * @param name      the name of the test data file
     * @param extension the extension of the test data file
     * @return a file object pointed to the input data: note that as this file
     *         is from the classpath it may not always be possible to use it
     *         directly (instead consider {@link #copyInputToDirectory(String, String, java.io.File)}
     */
    public File getInputFile(String name, String extension)
    {
        try
        {
            URL inputURL = getInputURL(name, extension);
            return new File(inputURL.toURI());
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns an input stream for test data located on the classpath, named
     * after the running test case and with the given extension.  Equivalent to
     * getInput(getName(), extension).
     *
     * @see #getInput(String, String)
     *
     * @param extension the extension of the test data file
     * @return an input stream open at the beginning of the found test data
     */
    public InputStream getInput(String extension)
    {
        return getInput(getName(), extension);
    }

    /**
     * Returns an input stream for test data located on the classpath, with the
     * given name and extension.  The test data is located using {@link Class#getResourceAsStream(String)},
     * with the name passed being &lt;simple classname&gt;.name.extension.  The
     * simplest way to use this method is to keep your test data file alongside
     * your test class and ensure it is "compiled" with the class to the
     * classpath.
     *
     * @see Class#getResourceAsStream(String)
     *
     * @param name      the name of the test data file
     * @param extension the extension of the test data file
     * @return an input stream open at the beginning of the test data
     */
    public InputStream getInput(String name, String extension)
    {
        String fullName = getClass().getSimpleName() + "." + name + "." + extension;
        InputStream stream = getClass().getResourceAsStream(fullName);
        if (stream == null)
        {
            fail("Required input '" + fullName + "' not found");
        }
        return stream;
    }

    /**
     * Returns a URL pointing to a test data file located on the classpath
     * named after the running test case and with the given extension.
     * Equivalent to getInputURL(getName(), extension).
     *
     * @see #getInputURL(String, String)
     *
     * @param extension extension of the test data file
     * @return a URL pointing to the test data file on the classpath
     */
    public URL getInputURL(String extension)
    {
        return getInputURL(getName(), extension);
    }

    /**
     * Returns a URL pointing to a test data file with the given name and
     * extension.  The file is located using {@link Class#getResource(String)},
     * with the name passed being &lt;simple classname&gt;.name.extension.
     * The simplest way to use this method is to keep your test data file
     * alongside your test class and ensure it is "compiled" with the class to
     * the classpath.
     *
     * @see Class#getResource(String)
     *
     * @param name      the name of the test data file
     * @param extension the extension of the test data file
     * @return a URL pointinf to the test data file on the classpath
     */
    public URL getInputURL(String name, String extension)
    {
        return getClass().getResource(getClass().getSimpleName() + "." + name + "." + extension);
    }

    /**
     * Copies a test data file from its location on the classpath to a new file
     * in the given directory.  Equivalent to copyInputToDirectory(getName(), extension, directory).
     *
     * @param extension the extension of the test data file
     * @param directory directory to copy the data file to
     * @return the new file that holds the test data (named after the data on
     *         the classpath)
     * @throws IOException if there is an error creating the file from the data
     */
    public File copyInputToDirectory(String extension, File directory) throws IOException
    {
        return copyInputToDirectory(getName(), extension, directory);
    }

    /**
     * Copies a test data file from its location on the classpath to a new file
     * in the given directory.  The data file is located on the classpath using
     * {@link #getInput(String, String)}, then copied to a new file with the
     * name name.extension in the given directory.
     *
     * @see #getInput(String, String)
     *
     * @param name      the name of the test data file
     * @param extension the extension of the test data file
     * @param directory directory to copy the data file to
     * @return the new file that holds the test data (named after the data on
     *         the classpath)
     * @throws IOException if there is an error creating the file from the data
     */
    public File copyInputToDirectory(String name, String extension, File directory) throws IOException
    {
        File destinationFile = new File(directory, name + "." + extension);
        IOUtils.joinStreams(getInput(name, extension), new FileOutputStream(destinationFile));
        return destinationFile;
    }

    /**
     * Removes a directory and all of its contents, ensuring that the removal
     * is successful.
     *
     * @param dir the directory to remove
     * @throws IOException if there is an error removing a file (some of the
     *         contents remain)
     */
    public static void removeDirectory(File dir) throws IOException
    {
        if (!FileSystemUtils.rmdir(dir))
        {
            throw new IOException("Failed to remove " + dir);
        }
    }
}
