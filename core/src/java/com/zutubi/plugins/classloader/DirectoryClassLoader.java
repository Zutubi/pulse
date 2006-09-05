package com.zutubi.plugins.classloader;

import com.zutubi.plugins.utils.IOUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * <class-comment/>
 */
public class DirectoryClassLoader extends PluginsClassLoader
{
    private File dir = null;

    public DirectoryClassLoader(File dir, ClassLoader parent)
    {
        super(parent);
        this.dir = dir;
    }

    protected URL getDataURL(String name, byte[] data) throws MalformedURLException
    {
        return new URL(null, dir.toURL().toExternalForm() + name, new BytesURLStreamHandler(data));
    }

    protected byte[] getFile(String path)
    {
        InputStream input = null;
        try
        {
            File f = new File(dir, path);
            if (!f.isFile())
            {
                return null;
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();

            input = new FileInputStream(f);
            byte[] buffer = new byte[1024];
            int n;

            while ((n = input.read(buffer)) > 0)
            {
                output.write(buffer, 0, n);
            }
            return output.toByteArray();
        }
        catch (IOException e)
        {
            return null;
        }
        finally
        {
            // ensure that we close the jar inputStream.
            IOUtils.close(input);
        }
    }

    public Object clone()
    {
        DirectoryClassLoader cl = new DirectoryClassLoader(dir, getParent());
        cl.packages = this.packages;
        return cl;
    }
}
