/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.core.plugins;

import com.zutubi.util.io.FileSystemUtils;
import com.zutubi.util.io.IOUtils;
import org.eclipse.osgi.framework.util.Headers;
import org.osgi.framework.BundleException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * An implementation of the local plugin that represents a bundle deployed
 * from a jar file.
 */
public class JarFilePlugin extends LocalPlugin
{
    public JarFilePlugin(File source)
    {
        super(source);
    }

    protected Headers loadPluginManifest(File pluginFile)
    {
        try
        {
            JarFile jarFile = null;
            try
            {
                jarFile = new JarFile(pluginFile);
                JarEntry entry = jarFile.getJarEntry("META-INF/MANIFEST.MF");
                if (entry == null)
                {
                    throw new IllegalArgumentException("No manifest found");
                }

                InputStream manifestIn = jarFile.getInputStream(entry);
                try
                {
                    return parseManifest(manifestIn);
                }
                finally
                {
                    IOUtils.close(manifestIn);
                }
            }
            finally
            {
                IOUtils.close(jarFile);
            }
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException(e);
        }
        catch (BundleException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    protected boolean delete()
    {
        try
        {
            FileSystemUtils.delete(source);
            return true;
        }
        catch (IOException e)
        {
            return false;
        }
    }

}
