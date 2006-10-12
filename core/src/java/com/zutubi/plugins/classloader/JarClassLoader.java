package com.zutubi.plugins.classloader;

import com.zutubi.plugins.utils.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 *
 * (adapted from tonic source code)
 * User: Hani Suleiman & Mike Cannon-Brookes
 * (originally sort-of copied from WebWork1 source)
 */
public class JarClassLoader extends PluginsClassLoader
{
    private JarFile jar;
    private File file;

    public JarClassLoader(File file, ClassLoader parent)
    {
        super(parent);
        this.file = file;
    }

    protected URL getDataURL(String name, byte[] data) throws MalformedURLException
    {
        return new URL(null, file.toURI().toURL().toExternalForm() + '!' + name, new BytesURLStreamHandler(data));
    }

    private void openJar() throws IOException
    {
        if (jar == null)
        {
            jar = new JarFile(file);
        }
    }

    public synchronized byte[] getFile(String path)
    {
        InputStream in = null;
        try
        {
            openJar();
            ZipEntry entry = jar.getEntry(path);
            if (entry == null)
            {
                return null;
            }

            in = jar.getInputStream(entry);
            int size = (int) entry.getSize();
            return readStream(in, size);
        }
        catch (IOException e)
        {
            return null;
        }
        finally
        {
            // ensure that we close the jar inputStream.
            IOUtils.close(in);
        }
    }

    public Object clone()
    {
        JarClassLoader loader = new JarClassLoader(file, getParent());
        loader.packages = packages;
        return loader;
    }

    /**
     * Close the jar open jar file.
     */
    public void close()
    {
        try
        {
            if (jar != null)
            {
                jar.close();
            }

            jar = null;
        }
        catch (IOException e)
        {
            System.err.println("Error closing JAR: " + e);
        }
    }
}