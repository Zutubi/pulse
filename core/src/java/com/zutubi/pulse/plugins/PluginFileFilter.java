package com.zutubi.pulse.plugins;

import com.zutubi.util.IOUtils;
import com.zutubi.pulse.util.FileSystemUtils;

import java.io.FileFilter;
import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

/**
 *
 *
 */
public class PluginFileFilter implements FileFilter
{
    public boolean accept(File file)
    {
        if (file.isFile() && file.getName().endsWith(".jar"))
        {
            // check that the required manifest file is present.
            JarFile jarFile = null;
            try
            {
                jarFile = new JarFile(file);
                return jarFile.getJarEntry("META-INF/MANIFEST.MF") != null;
            }
            catch (IOException e)
            {
                return false;
            }
            finally
            {
                IOUtils.close(jarFile);
            }
        }
        else if (file.isDirectory())
        {
            // do a simple check for the required plugin manifest file.
            File manifestFile = new File(file, FileSystemUtils.composeFilename("META-INF", "MANIFEST.MF"));
            return manifestFile.isFile();
        }
        return false;
    }   
}
